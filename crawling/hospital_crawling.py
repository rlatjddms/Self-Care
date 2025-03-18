import time
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By
import pandas as pd

# 웹드라이버 설정
driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()))

# 군산시 보건소 페이지 URL 리스트
urls = {
    "병원": "https://www.gunsan.go.kr/health/m1635",
    "의원": "https://www.gunsan.go.kr/health/m1636",
    "치과의원": "https://www.gunsan.go.kr/health/m1637",
    "한의원": "https://www.gunsan.go.kr/health/m1638",
}

# 병원 정보를 담을 리스트 초기화
all_hospital_data = []

for category, url in urls.items():
    # 각 카테고리 페이지로 이동
    driver.get(url)
    time.sleep(2)  # 페이지 로딩 대기

    # 표 데이터에서 <thead>와 <tbody>의 모든 행을 가져오기
    rows = driver.find_elements(By.CSS_SELECTOR, "#containertab thead tr") + \
           driver.find_elements(By.CSS_SELECTOR, "#containertab thead + tbody tr")

    for row in rows:
        columns = row.find_elements(By.TAG_NAME, "td")
        
        if len(columns) > 3:  # 3개의 열 이상이 존재할 때 (병원)
            # 각 열에서 의료기관명, 의료기관주소, 의료기관전화번호 추출
            hospital_name = columns[1].text.strip()  # 첫 번째 <td>에서 의료기관명
            hospital_address = columns[2].text.strip()  # 두 번째 <td>에서 의료기관주소
            hospital_phone = columns[3].text.strip()  # 세 번째 <td>에서 의료기관전화번호

            # 데이터 리스트에 추가
            all_hospital_data.append({
                "카테고리": category,
                "의료기관명": hospital_name,
                "의료기관주소": hospital_address,
                "의료기관전화번호": hospital_phone
            })

        elif len(columns) == 3:  # 3개의 열만 존재할 때 (의원, 치과의원, 한의원)
            # 각 열에서 의료기관명, 의료기관주소, 의료기관전화번호 추출
            hospital_name = columns[0].text.strip()  # 첫 번째 <td>에서 의료기관명
            hospital_address = columns[1].text.strip()  # 두 번째 <td>에서 의료기관주소
            hospital_phone = columns[2].text.strip()  # 세 번째 <td>에서 의료기관전화번호

            # 데이터 리스트에 추가
            all_hospital_data.append({
                "카테고리": category,
                "의료기관명": hospital_name,
                "의료기관주소": hospital_address,
                "의료기관전화번호": hospital_phone
            })

# 웹드라이버 종료
driver.quit()

# 데이터를 pandas DataFrame으로 변환
df = pd.DataFrame(all_hospital_data)

# 데이터를 'hospital.xlsx' 엑셀 파일로 저장
df.to_excel("hospital.xlsx", index=False)

print("모든 병원 정보가 hospital.xlsx 파일에 저장되었습니다.")
