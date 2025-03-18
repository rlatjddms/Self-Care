from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
import pandas as pd
import time
import mysql.connector

# 크롬 드라이버 설정
chrome_options = Options()
chrome_options.add_argument("--headless")  # Headless 모드로 실행
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")

# 크롬 드라이버 경로 설정 (webdriver-manager 사용하여 자동 설치)
service = Service(ChromeDriverManager().install())
driver = webdriver.Chrome(service=service, options=chrome_options)

# 크롤링할 URL
base_url = 'http://www.snuh.org/health/nMedInfo/nList.do'
driver.get(base_url)

# 각 페이지에서 링크를 추출합니다.
links = []

def extract_links():
    try:
        # <div class="thumbType04"> 요소가 로드될 때까지 대기
        thumb_div = WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CLASS_NAME, 'thumbType04'))
        )

        # <div class="thumbType04"> 안에 있는 <div class="item"> 요소들을 찾습니다.
        items = thumb_div.find_elements(By.CLASS_NAME, 'item')

        # 각 <div class="item"> 안의 링크를 추출합니다.
        for item in items:
            a_tag = item.find_element(By.TAG_NAME, 'a')  # <a> 태그를 찾습니다.
            link = a_tag.get_attribute('href')  # href 속성을 추출합니다.
            links.append(link)
    except Exception as e:
        print(f"Failed to extract links on this page: {e}")

page_number = 1

while True:
    print(f"Processing page {page_number}")
    extract_links()
    
    try:
        # '다음 페이지' 버튼을 찾고 클릭합니다.
        next_button = driver.find_element(By.CSS_SELECTOR, f'a[href="javascript:submitSearchForm({page_number + 1})"]')
        next_button.click()
        page_number += 1
        time.sleep(2)  # 페이지 로드 대기
    except Exception as e:
        print(f"No more pages to process: {e}")
        break  # 더 이상 다음 페이지가 없으면 종료

# 각 링크에 접속하여 필요한 정보를 추출합니다.
data = []
for link in links:
    driver.get(link)
    entry = {'URL': link}

    # 제목 추출
    try:
        view_title_div = WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CLASS_NAME, 'viewTitle'))
        )
        h3_tag = view_title_div.find_element(By.TAG_NAME, 'h3')
        title_html = h3_tag.get_attribute('innerHTML').replace('<br>', '').strip()  # <br> 태그 제거 및 양쪽 공백 제거
        title_text = ' '.join(title_html.split())  # 모든 연속된 공백을 하나의 공백으로 변환
        entry['제목'] = title_text
    except:
        entry['제목'] = "N/A"  # 제목을 찾지 못한 경우 "N/A"로 표시

    # 진단용 증상 추출
    try:
        em_related_symptom = driver.find_element(By.XPATH, "//em[text()='관련 증상']")
        diagnosis_symptom_p = em_related_symptom.find_element(By.XPATH, "./following-sibling::p")
        diagnosis_symptoms = diagnosis_symptom_p.text.replace(" , ", ",").replace(" ,", ",").replace(", ", ",").strip()
        entry['진단용 증상'] = diagnosis_symptoms
    except:
        entry['진단용 증상'] = ""

    # 정의, 증상, 원인, 대처법 추출
    detail_wrap_div = driver.find_element(By.CLASS_NAME, 'detailWrap')
    categories = ['정의', '증상', '원인', '생활가이드']
    for category in categories:
        try:
            h5_tag = detail_wrap_div.find_element(By.XPATH, f".//h5[text()='{category}']")
            p_tag = h5_tag.find_element(By.XPATH, "./following-sibling::p")
            if category == '생활가이드':
                entry['대처법'] = p_tag.text.strip()
            else:
                entry[category] = p_tag.text.strip()
        except:
            if category == '생활가이드':
                entry['대처법'] = "근처 병원에 들러 의사에게 진료받으세요"
            else:
                entry[category] = ""

    # 진료과 추출
    try:
        em_clinic = driver.find_element(By.XPATH, "//em[contains(text(),'진료과')]")
        clinic_p = em_clinic.find_element(By.XPATH, "./following-sibling::p")
        clinics = clinic_p.text.replace(" , ", ",").replace(" ,", ",").replace(", ", ",").replace(", ", ",").strip()
        entry['진료과'] = clinics
    except:
        entry['진료과'] = ""

    # '정의', '증상', '원인'이 모두 빈칸인 경우 항목을 제외합니다.
    if not (entry['정의'] == "" and entry['증상'] == "" and entry['원인'] == ""):
        data.append(entry)

# 드라이버 종료
driver.quit()

# 데이터프레임으로 변환
df = pd.DataFrame(data)

# MySQL에 저장
def save_to_mysql(data):
    # MySQL 연결 설정
    db_connection = mysql.connector.connect(
        host='localhost',  # MySQL 서버 주소
        user='root',  # MySQL 사용자 이름
        password='kse020626',  # MySQL 사용자 비밀번호
        database='capstone'  # 사용할 데이터베이스 이름
    )
    cursor = db_connection.cursor()

    # 데이터 삽입 쿼리
    insert_query = """
    INSERT INTO disease (diseaseName, diagnosis, definition, symptom, cause, management, medicalSubject, category)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """
    
    # 데이터 삽입
    for entry in data:
        cursor.execute(insert_query, (
            entry['제목'], 
            entry['진단용 증상'], 
            entry['정의'], 
            entry['증상'], 
            entry['원인'], 
            entry['대처법'], 
            entry['진료과'], 
            entry['URL']
        ))
    
    # 커밋 및 연결 종료
    db_connection.commit()
    cursor.close()
    db_connection.close()

# 데이터 저장
save_to_mysql(data)

print("데이터가 MySQL 데이터베이스에 저장되었습니다.")
