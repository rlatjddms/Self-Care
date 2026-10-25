# 🩺 Self-Care

> **사용자 스스로 건강 상태를 확인하고 질환을 예측할 수 있는 모바일 헬스케어 앱**

| 항목 | 내용 |
|---|---|
| 기간 | 2024.04 ~ 2024.09 (약 5개월) |
| 팀 | 1인 개발 (개인 프로젝트) |

## 기획 의도
- 병원 방문이 어려운 현대인들이 일상에서 간편하게 건강 상태를 확인하고 조기 예방을 도울 수 있는 디지털 헬스케어 도구 필요
- 가벼운 증상이라도 적시에 확인하여 불필요한 시간과 비용을 줄이고, 일상적인 건강 관리를 돕는 서비스의 필요성 체감

## 주요 기능
1. **질병 자가 진단**: 증상 선택에 따라 문진과 결과가 이어지는 데이터 로직 (MariaDB 기반 구조 설계)
2. **AI 간편 진료**: OpenAI API 연동 챗봇으로 건강·질병 관련 질의응답 처리
3. **사진으로 피부 질환 진단**: Teachable Machine으로 약 80종 피부 질환 이미지 모델을 학습하고, TensorFlow Lite로 온디바이스 분석
4. **오늘의 건강 뉴스**: Python 크롤링 파이프라인으로 최신 건강 정보 제공

## 기술 스택
| 영역 | 기술 |
|---|---|
| App | Java, XML, Android Studio |
| Backend / DB | PHP, MariaDB, phpMyAdmin, JSON |
| AI / 외부 API | OpenAI API, Teachable Machine, TensorFlow Lite, Naver Cloud Platform |
| 데이터 수집 | Python, Selenium, Pandas, Jsoup |

## 폴더 구조
```
Self-Care
├── AndroidStudio/Capstone_Hospital   # 안드로이드 앱
├── TensorflowLite                    # 피부 질환 분류 모델
├── crawling                          # 건강 뉴스 크롤링
└── php                               # 서버 API (PHP + MariaDB)
```

## 스크린샷
<img width="800" alt="image" src="https://github.com/user-attachments/assets/a0dab072-e5b6-489e-9d00-52c004be5e94" />
