# GitHub 원터치 업데이트 배포

HanClip Android는 Google Play 대신 공식 GitHub Release의 APK를 확인한다. 소스 `git push`만으로는
업데이트가 배포되지 않는다. 검증한 APK를 아래 규칙의 안정 Release로 공개해야 기존 설치본에 안내된다.

## 앱의 확인 규칙

- API: `https://api.github.com/repos/armsone/HanClip-Android/releases/latest`
- 태그: `android-v{versionCode}` (예: `android-v544`)
- APK 이름: `HanClip-Android-v{versionCode}.apk`
- draft와 prerelease는 무시한다.
- 현재 `versionCode`보다 큰 버전만 안내한다.
- HTTPS 저장소 주소, 파일 크기, package name `com.hanclip.android`, APK `versionCode`와 현재
  설치본의 서명이 모두 일치해야 설치 화면을 연다.
- 앱 시작 시 확인만 하며 다운로드와 설치 화면은 사용자가 각각 선택해야 진행한다.

## 배포 절차

1. 변경 관련 JVM 시험, `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug`와 지정 실기기 검증을 마친다.
2. APK가 업데이트 계보와 같은 인증서인지 `apksigner verify --print-certs`로 확인한다.
3. `versionCode`가 `N`이면 검증한 APK를 `HanClip-Android-vN.apk`로 준비한다.
4. `gh release create android-vN HanClip-Android-vN.apk --title "HanClip Android vN" --notes ...`로 공개한다.
5. GitHub API에서 안정 Release의 태그, 자산 이름·크기·다운로드 주소를 다시 확인한다.
6. 낮은 `versionCode`의 설치본에서 안내→다운로드→알 수 없는 앱 설치 권한→Android 설치 화면과
   기존 영화·설정 보존을 확인한다. 앱 삭제나 데이터 초기화는 하지 않는다.

Android 10에서 PackageManager가 임시 APK를 검사할 수 있도록 다운로드 파일도 `.partial.apk`로
끝낸다. Samsung Android 10의 archive 인증서 호환성을 위해 minSdk 26 전체의 `GET_SIGNATURES`로
설치본과 다운로드 APK의 인증서 전체 일치를 확인한다.

## 서명 보관

현재 직접 설치 업데이트 계보는 이 Mac의 Android debug keystore를 사용한다. keystore나 비밀번호를
저장소·Release·문서에 넣지 않는다. 다른 인증서로 서명하면 기존 설치본 위에 업데이트할 수 없다.
Google Play 서명으로 전환할 때는 별도 서명 이전 계획을 먼저 확정한다.
