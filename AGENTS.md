# HanClip Android Codex 안내

이 저장소에서 작업을 시작하기 전에 루트의 `PROJECT_RULES.md`를 끝까지 읽고 따른다.
이 저장소는 HanClip Android 전용이며, 별도 요청 없이 형제 경로의 iOS 저장소를 수정하지 않는다.
`/Users/armsone/git/HanClip`과 `ios-source-readonly`는 항상 읽기 전용이며 사용자 요청이 있어도 이 프로젝트 작업에서는 수정하지 않는다.

## 사용자 실기기 데이터 보호

- 사용자 데이터가 있는 실기기에서는 Gradle `connectedDebugAndroidTest`를 실행하지 않는다. 대상 앱 제거·재설치로 앱 내부 프로젝트와 설정이 지워질 수 있다.
- 실기기 계측이 꼭 필요하면 대상 앱을 제거하지 않는 개별 test APK 설치와 `am instrument` 방식만 사용한다. 데이터 보존을 확인할 수 없으면 JVM 시험·컴파일·수동 검증으로 대체한다.
