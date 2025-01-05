나의 당뇨수첩 📱
![App Logo](앱 로고 이미지 URL)
📋 소개
당신의 건강한 혈당 관리 파트너
일상적인 혈당 관리부터 진료 기록까지, 당뇨 관리에 필요한 모든 것을 한 앱에서 관리할 수 있습니다.
💫 주요 기능
🏠 홈

일일 혈당 기록 (공복/식전/식후)
체중 모니터링
주간 혈당 추이 그래프
직관적인 데이터 입력

📝 기록

전체 기록 타임라인
특이사항 메모
간편한 데이터 관리
일별 기록 삭제

🏥 메디노트

진료 기록 관리

병원명 및 진료일자
상세 처방 내용
다음 예약일 설정


최근 진료 히스토리

📊 분석

기간별 혈당 분석

주간/월간/분기/반기/연간


평균 혈당 분포도
혈당 통계

평균/최고/최저



⚙️ 설정

맞춤 알림 설정

기상 시간
식전 체크 알림


데이터 관리

CSV 내보내기
데이터 초기화



🛠️ 기술 스택
Core
Show Image
Show Image
Show Image
Architecture

MVVM Pattern
Repository Pattern
Navigation Component

Libraries
gradleCopy// 데이터베이스
implementation 'androidx.room:room-runtime:2.5.2'
implementation 'androidx.room:room-ktx:2.5.2'

// 차트
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

// 비동기 처리
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'

// CSV 내보내기
