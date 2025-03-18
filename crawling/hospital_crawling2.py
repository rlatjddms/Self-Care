import time
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import pandas as pd

# 웹드라이버 설정
driver_path = "C:/Users/seongeun/Desktop/chromedriver-win64/chromedriver.exe"  # ChromeDriver 경로 지정
service = Service(executable_path=driver_path)
driver = webdriver.Chrome(service=service)

# 네이버 지도 페이지 URL
naver_map_url = "https://map.naver.com/p?c=15.00,0,0,0,dh"

# 크롤링할 병원 데이터 불러오기 (엑셀 파일 또는 DataFrame)
df = pd.read_excel("hospital.xlsx")

# 진료과목 및 URL 데이터를 담을 리스트
medical_subject_data = []

for index, row in df.iterrows():
    hospital_name = row['의료기관명']
    search_query = f"군산 {hospital_name}"

    # 네이버 지도 페이지로 이동
    driver.get(naver_map_url)
    time.sleep(2)  # 페이지 로딩 대기

    # 검색창에 의료기관명 입력 후 검색
    search_box = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located((By.CSS_SELECTOR, "div.input_box > input"))
    )
    search_box.send_keys(search_query)
    search_box.send_keys(Keys.RETURN)
    time.sleep(2)  # 검색 결과 로딩 대기

    try:
        # searchIframe이 있는지 확인
        search_iframe_parent = WebDriverWait(driver, 5).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "div.sc-48msce.bcmMFw"))
        )
        try:
            # searchIframe으로 전환 후 병원 정보 클릭
            WebDriverWait(driver, 5).until(
                EC.frame_to_be_available_and_switch_to_it((By.ID, "searchIframe"))
            )

            # 검색 결과 중 첫 번째 병원 정보 클릭 (첫 번째 결과를 클릭)
            first_result_element = WebDriverWait(driver, 5).until(
                EC.element_to_be_clickable((By.XPATH, "//a[contains(@class, 'P7gyV')]"))
            )
            driver.execute_script("arguments[0].click();", first_result_element)
            time.sleep(2)

            # entryIframe으로 전환
            driver.switch_to.default_content()
            WebDriverWait(driver, 10).until(
                EC.frame_to_be_available_and_switch_to_it((By.ID, "entryIframe"))
            )

        except Exception as e:
            # searchIframe이 없으면 바로 entryIframe으로 전환
            print(f"{hospital_name} - searchIframe이 없어 바로 entryIframe으로 전환합니다.")
            driver.switch_to.default_content()
            WebDriverWait(driver, 10).until(
                EC.frame_to_be_available_and_switch_to_it((By.ID, "entryIframe"))
            )

        # entryIframe에서 진료과목을 포함하는 <h3> 태그가 '진료과목'인지 확인
        medical_subject_section = WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.XPATH, "//div[@class='DAQTB']//h3[text()='진료과목']"))
        )

        # 진료과목 목록을 포함하는 <ul> 태그 내부의 <li> 태그를 찾음
        medical_subject_list = medical_subject_section.find_elements(By.XPATH, ".//following-sibling::ul[@class='xrrcZ']//li[@class='zxtJF']")

        # 진료과목 리스트로 저장
        subjects = [subject.text for subject in medical_subject_list]

        # 현재 페이지 URL 저장
        current_url = driver.current_url

        # 진료과목과 URL을 병원 데이터에 추가
        medical_subject_data.append({
            "의료기관명": hospital_name,
            "진료과목": ", ".join(subjects),
            "url": current_url
        })

    except Exception as e:
        print(f"{hospital_name} - 진료과목을 찾을 수 없습니다: {e}")
        medical_subject_data.append({
            "의료기관명": hospital_name,
            "진료과목": "정보 없음",
            "url": "정보 없음"
        })

    finally:
        driver.switch_to.default_content()

# 웹드라이버 종료
driver.quit()

# 진료과목 데이터를 DataFrame으로 변환
subject_df = pd.DataFrame(medical_subject_data)

# 기존 데이터와 병합
final_df = pd.merge(df, subject_df, on="의료기관명")

# 최종 데이터를 엑셀 파일로 저장
output_file = "hospital_with_subjects_and_urls.xlsx"
final_df.to_excel(output_file, index=False)

# 저장 완료 메시지 출력
print(f"모든 병원의 진료과목 정보와 URL이 '{output_file}' 파일에 저장되었습니다.")
