# HanClip Android 저장소 분리 기록

- 분리일: 2026-08-10
- 새 저장소: `/Users/armsone/git/HanClip-Android`
- 원본 통합 저장소: `/Users/armsone/git/HanClip`
- 분리 기준 원본 커밋: `da55f9a9bbfffd5a5b5107d4ef741da171f1e81f`

## 보존 내용

- `android/` 경로의 기존 Git 이력을 저장소 루트 이력으로 변환했다.
- 분리 시점의 미커밋 Android 소스 변경을 새 저장소에 함께 보존했다.
- 애플리케이션 ID `com.hanclip.android`, 버전명과 기존 저장 형식은 변경하지 않았다.

## 독립 빌드 변경

- iOS 프로젝트에서 빌드 때 복사하던 폰트 3개, 폰트 라이선스와 컬렉션 핀 이미지를 `app/src/main/assets`로 옮겼다.
- Gradle의 `../HanClip/...` 상대경로 자산 동기화 작업을 제거했다.
- 현재 iOS 비교 기준은 `reference/ios-current`에 스냅샷으로 보존했다.

## 원격 저장소

- 통합 저장소로 잘못 푸시하지 않도록 새 저장소의 기존 `origin`을 제거했다.
- Android 전용 원격 저장소가 준비되면 그 URL을 새 `origin`으로 등록한다.
