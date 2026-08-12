# HanClip iOS → Android 최종 소스 감사

- 기준 시점: 2026-08-13
- iOS 기준 원본: `/Users/armsone/git/HanClip` (`31e60ec`)
- Android 비교본: `/Users/armsone/git/HanClip-Android` (`98131db` 이후 수정 포함)
- 방법: 과거 완료표와 `IOS_PARITY.md`를 증거에서 제외하고, 현재 iOS 진입점·화면·상태·서비스를 소스에서 추적한 뒤 Android 현재 소스와 대조했다.
- 한계: 이 문서는 **소스 감사**다. iOS 빌드는 CoreSimulator 서비스가 차단된 환경에서 Asset Catalog 단계가 실패했으며, Android는 `testDebugUnitTest lintDebug`가 성공했다. 두 플랫폼 실기기 픽셀·제스처·TalkBack/VoiceOver·폴드 전환·저장 공간 부족 실험은 수행하지 못했다. 해당 항목은 아래에서 `미검증`을 명시하고 `동일`로 판정하지 않았다.
- 판정 의미: `동일`은 현재 소스에서 결과와 수치가 일치한 경우만, `부분`은 핵심 일부만 있거나 런타임 확인이 남은 경우, `누락`은 iOS 기능의 호출 가능한 Android 경로가 없는 경우, `잘못 구현`은 Android 동작·수치·결과가 iOS와 다른 경우, `플랫폼 고유`는 시스템 UI만 다르고 사용자 결과·상태 수명이 같은 경우다.

## Android 수정 진행 상태

- 소스 수정 및 단위시험·빌드·린트 완료: A04, A05, A07, A09, A11, A12, A13, B05, B06, C03, C05, C06, C09, C10, C12, C15, C16, C17, C19, D01, D02, D04, D05, D10, D11, D12, D13, D14, E03, E04, E06, E07, E11, F01, F05, F06.
- SM-F968N에 APK를 설치하고 A05 공유 사진 복사→새 영화/기존 영화/비우기 선택→새 영화 Live 클립 가져오기를 확인했다. B06·E04·E11의 실제 gesture/lifecycle 결과와 미디어 출력 결과는 아직 검증하지 않았으므로 해당 항목을 `동일`로 재판정하지 않는다.
- E02는 기기 지원 범위 버튼, 로그 정밀 zoom, 0.5초 자동 닫힘을 소스와 단위시험에 반영했다. 물리 렌즈 전환의 플랫폼별 결과만 실기기 비교가 남았다.
- 결제 A14는 결제 상품·외부 설정 승인이 필요한 별도 작업이라 수정하지 않았다.

## 감사 항목

각 항목은 요구된 7개 필드를 같은 순서로 기록한다.

### A. 앱 진입점·홈·설정

#### A01 — 앱 루트와 편집 진입
1. **iOS 실제 동작/수치:** `isProjectOpen`이 false면 홈, true면 편집을 표시하며 홈의 프리셋·사진·달력·파일에서 편집으로 진입한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:287-313, 674-701`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/HanClipApp.kt:241-369`
4. **판정:** 동일
5. **보이는 차이:** 진입 결과 차이 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 콜드 런치 후 `새 영화→사진`을 눌러 편집 화면이 열리는지 확인한다.

#### A02 — 외부 호출 주소 7종
1. **iOS 실제 동작/수치:** `open/aishot/photo/quick/calendar/files/search` 7개 `hanclip://` 주소를 처리한다.
2. **iOS 근거:** `HanClip/App/HanClipApp.swift:615-665`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/navigation/HanClipQuickAction.kt:5-27`, `app/src/main/java/com/hanclip/android/HanClipApp.kt:170-211`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 7개 URI를 각각 실행해 지정 화면이 한 번씩 열리는지 확인한다.

#### A03 — 앱 아이콘 빠른 작업
1. **iOS 실제 동작/수치:** 실제 등록된 앱 아이콘 shortcut은 사진·퀵모드·AiShot 3종이다. 라우터는 6종을 해석하지만 달력·파일·검색은 Info.plist에 등록되지 않아 앱 아이콘 진입점이 아니다.
2. **iOS 근거:** `HanClip/Info.plist:84-109`, `HanClip/App/HanClipApp.swift:648-665`
3. **Android 근거:** `app/src/main/res/xml/shortcuts.xml:3-38`
4. **판정:** 동일
5. **보이는 차이:** 두 플랫폼 모두 사진·퀵모드·AiShot 3종이다.
6. **수정 파일/영역:** 없음.
7. **시험:** 런처에서 앱 아이콘을 길게 눌러 3종 기능 진입을 모두 확인한다.

#### A04 — 위젯
1. **iOS 실제 동작/수치:** 잠금 화면 원형 위젯 하나가 `HanClip 열기`로 `hanclip://open`을 호출한다.
2. **iOS 근거:** `HanClipWidget/HanClipLockWidget.swift:29-69`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/widget/HanClipQuickWidgetProvider.kt:13-51`, `app/src/main/res/layout/hanclip_quick_widget.xml:1-79`
4. **판정:** 부분
5. **보이는 차이:** Android 위젯은 사진·퀵·AiShot만 제공하고 일반 `HanClip 열기`가 없다.
6. **수정 파일/영역:** `HanClipQuickWidgetProvider.kt` 클릭 라우팅과 `hanclip_quick_widget.xml` 일반 열기 영역.
7. **시험:** 위젯의 HanClip 본체 영역을 눌러 홈이 열리는지 확인한다.

#### A05 — 공유 확장/외부 공유 수신
1. **iOS 실제 동작/수치:** 별도 공유 확장에서 로고·썸네일·복사 진행률·상태·취소·`HanClip에서 열기`를 보여준 뒤 호스트로 넘긴다.
2. **iOS 근거:** `HanClipShare/ShareViewController.swift:6-180`
3. **Android 근거:** `app/src/main/AndroidManifest.xml:118-136`, `app/src/main/java/com/hanclip/android/HanClipApp.kt:96-124`
4. **판정:** 부분
5. **보이는 차이:** Android는 시스템 공유 후 곧바로 앱으로 들어오며 iOS의 복사 진행·취소·완료 화면이 없다.
6. **수정 파일/영역:** `MainActivity.kt` 공유 Intent 수신과 `HanClipApp.kt` 공유 inbox 화면 상태.
7. **시험:** 2GB 영상을 다른 앱에서 공유하고 진행률·취소·완료 후 재개를 확인한다.

#### A06 — 프리셋 목록
1. **iOS 실제 동작/수치:** 새 영화·퀵모드·AiShot·여행 영화·인생 영화·골프 영화 6종이다.
2. **iOS 근거:** `HanClip/ViewModels/EditorViewModel.swift:739-790`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/model/MoviePreset.kt:3-30`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 홈에 6개 프리셋이 한 번씩만 표시되는지 확인한다.

#### A07 — 프리셋별 수치와 로고
1. **iOS 실제 동작/수치:** 여행 1초·1/6·분할·여행 음악·로고 끔, 인생 2초·1/3·분할, 골프 2초·1/3·분할·골프 음악, AiShot 로고 끔이다.
2. **iOS 근거:** `HanClip/ViewModels/EditorViewModel.swift:749-798`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorViewModel.kt:2082-2195`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android AiShot은 로고를 켜고 좌하단, 여행은 로고를 켜고 하단 중앙에 둔다.
6. **수정 파일/영역:** `EditorViewModel.kt`의 `applyPresetDefaults` AiShot·Travel watermark 분기.
7. **시험:** AiShot과 여행 프리셋을 각각 새로 열어 로고가 꺼져 있는지 확인한다.

#### A08 — 프로젝트·AiShot·핀 한도
1. **iOS 실제 동작/수치:** 전체 10개, AiShot 2개, 핀 5개다.
2. **iOS 근거:** `HanClip/Services/ProjectStore.swift:38-41, 68-85, 475-490`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/project/DraftProjectStore.kt:191-200, 282-317`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 11번째 일반·3번째 AiShot·6번째 핀을 순서대로 시도해 한도를 확인한다.

#### A09 — 프로젝트 메모
1. **iOS 실제 동작/수치:** 앞뒤 공백만 제거하며 글자 수 제한은 없다.
2. **iOS 근거:** `HanClip/Services/ProjectStore.swift:504-514`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/home/HomeRoute.kt:689-745`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 입력 중 80자에서 잘린다.
6. **수정 파일/영역:** `HomeRoute.kt`의 두 `onValueChange { memoText = it.take(80) }`.
7. **시험:** 81자 메모를 저장·재실행해 81자가 모두 남는지 확인한다.

#### A10 — 편집 저장·홈·취소
1. **iOS 실제 동작/수치:** `저장 후 홈`, `저장`, `홈` 세 액션이며 `홈`은 현재 메모리 변경을 버린다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:2382-2441, 2534-2598`, `HanClip/ViewModels/EditorViewModel.swift:1719-1739`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:817-875`, `EditorViewModel.kt:960-980`
4. **판정:** 동일
5. **보이는 차이:** 정상적으로 팝업을 거친 흐름은 동일하다.
6. **수정 파일/영역:** 없음.
7. **시험:** 저장된 프로젝트를 수정한 뒤 `홈`을 눌러 이전 저장 상태로 돌아오는지 확인한다.

#### A11 — 앱 종료·재실행 후 미저장 상태
1. **iOS 실제 동작/수치:** 명시적 저장 전 편집은 ProjectStore에 쓰지 않으므로 강제 종료 후 이전 저장본이 유지된다.
2. **iOS 근거:** `HanClip/ViewModels/EditorViewModel.swift:1724-1768`, `HanClip/Views/EditorView.swift:1125-1139`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:367-385`, `EditorViewModel.kt:937-980`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 편집 때마다 자동 저장해 정상 `홈` 롤백 전에 프로세스가 죽으면 미저장 변경이 다음 실행에 남는다.
6. **수정 파일/영역:** `EditorRoute.kt` 자동 저장 `LaunchedEffect`와 `EditorViewModel.kt` 세션 journal/commit 경계.
7. **시험:** 저장본 수정→앱 강제 종료→재실행 후 수정 전 값이 열리는지 확인한다.

#### A12 — 테마 기본값과 팔레트
1. **iOS 실제 동작/수치:** 자동 밝은 테마 배경은 흰색, 기본 글자는 `#1A1A1A`이며 iOS 색상 모드에 적응한다.
2. **iOS 근거:** `HanClip/App/HanClipApp.swift:5-165, 376, 573-589`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/theme/HanClipThemeMode.kt:9-181`, `HanClipTheme.kt:11-45`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android 자동 테마는 `#E7F0EE→#F8FBFA` 그라데이션과 `#0F172A` 글자를 사용한다.
6. **수정 파일/영역:** `HanClipThemeMode.kt` Automatic 팔레트와 `HanClipTheme.kt` Material colorScheme 연결.
7. **시험:** 두 앱을 밝은 자동 테마로 콜드 런치해 홈 배경·본문 색을 픽셀 샘플링한다.

#### A13 — 도움말/기능 사전
1. **iOS 실제 동작/수치:** 카피라이터에 Ai·AiShot·플레이어·브라우저·편집·저작권 등 전체 기능 설명과 정확한 동작 문구를 노출한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:8396-8479`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/home/HomeRoute.kt:1991-2087`
4. **판정:** 부분
5. **보이는 차이:** Android 문구는 축약됐고 브라우저 중복 정책·플레이어 제스처처럼 실제 차이를 Android식으로 설명한다. 두 플랫폼 모두 AiShot 짧게를 도움말에서 2초로 잘못 설명한다.
6. **수정 파일/영역:** `HomeRoute.kt`의 `importantInfoItems`; 기능 수정 뒤 iOS 문구와 동기화.
7. **시험:** 항목 제목과 본문을 추출해 iOS 목록과 문자열 diff가 0인지 확인한다.

#### A14 — 로고 제거 구매·복원
1. **iOS 실제 동작/수치:** 영구 `$9.99`, 1년 `$4.99`, 1달 `$0.99` fallback, 결제 pending/cancel/error, 복원, entitlement 갱신을 제공한다.
2. **iOS 근거:** `HanClip/Services/CopyrightPurchaseManager.swift:4-54, 109-160, 208-245`
3. **Android 근거:** `app/src/main/java` 및 `app/build.gradle.kts`에 BillingClient·구매·복원 구현 없음.
4. **판정:** 누락
5. **보이는 차이:** Android에서 구매·복원·구독 상태 수명을 사용할 수 없다.
6. **수정 파일/영역:** 새 `core/billing/CopyrightPurchaseManager.kt`, `HomeRoute.kt` 카피라이터 구매 UI, Gradle Play Billing 의존성.
7. **시험:** 테스트 상품 구매→앱 재설치→복원 후 로고 제거 entitlement가 유지되는지 확인한다.

### B. 사진·달력·파일 선택

#### B01 — 선택 가능한 미디어 종류
1. **iOS 실제 동작/수치:** 사진·Live Photo·영상이며 기본으로 세 종류를 모두 표시한다.
2. **iOS 근거:** `HanClip/Services/PhotoPicker.swift:429-454`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/CalendarMediaPickerSheet.kt:703-747, 2438-2445`
4. **판정:** 부분
5. **보이는 차이:** Android는 파일 헤더의 MotionPhoto/MicroVideo 문자열로 모션포토를 추정해 제조사 변형은 일반 사진으로 보일 수 있다. 실파일 호환성은 미검증이다.
6. **수정 파일/영역:** `core/media/MediaImportReader.kt:55-70,163-220` XMP container parser.
7. **시험:** Samsung·Google 모션포토 각 1개를 선택해 모두 `영상` 모드로 표시되는지 확인한다.

#### B02 — 사진 정렬
1. **iOS 실제 동작/수치:** 촬영일/추가일과 오름/내림을 토글하며 기본은 촬영일 오래된 순이다.
2. **iOS 근거:** `HanClip/Services/PhotoPicker.swift:429-454, 1068-1073`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/CalendarMediaPickerSheet.kt:730-742, 2179-2200`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 촬영일이 역전된 3개 항목으로 네 정렬 조합을 확인한다.

#### B03 — 영상 길이 필터
1. **iOS 실제 동작/수치:** 이상/이하, 최소 1초, 빠른 값 1·3·5·10분이며 적용 중 사진/Live를 숨기고 해제 때 이전 종류를 복원한다.
2. **iOS 근거:** `HanClip/Services/PhotoPicker.swift:967-971, 1123-1135`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/CalendarMediaPickerSheet.kt:1839-1953, 2187-2199`
4. **판정:** 동일
5. **보이는 차이:** 소스상 핵심 값과 복원 규칙은 같다.
6. **수정 파일/영역:** 없음.
7. **시험:** 사진 선택을 유지한 채 3분 이상 필터 적용·해제를 하고 이전 선택 종류가 복원되는지 확인한다.

#### B04 — 썸네일 열과 핀치
1. **iOS 실제 동작/수치:** 열 수는 1→3→5→8이며 핀치로 단계 이동한다.
2. **iOS 근거:** `HanClip/Services/PhotoPicker.swift:772-786, 1510-1535`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/safety/RecoveryPolicy.kt:16-23`, `CalendarMediaPickerSheet.kt:1538-1745`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 확대·축소 핀치를 반복해 1/3/5/8열만 순환하는지 확인한다.

#### B05 — 길게 눌러 미리보기
1. **iOS 실제 동작/수치:** 선택 여부와 무관하게 0.45초 길게 누르면 큰 미리보기가 열린다.
2. **iOS 근거:** `HanClip/Services/PhotoPicker.swift:772-786, 1762-1790`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/CalendarMediaPickerSheet.kt:1786-1803`
4. **판정:** 부분
5. **보이는 차이:** Android는 이미 선택된 항목에만 길게 누르기 미리보기가 연결된다.
6. **수정 파일/영역:** `CalendarMediaPickerSheet.kt` `combinedClickable`의 unselected 항목 long-click.
7. **시험:** 선택하지 않은 사진을 길게 눌러 미리보기가 열리는지 확인한다.

#### B06 — 드래그 다중 선택과 가장자리 자동 스크롤
1. **iOS 실제 동작/수치:** 그리드를 드래그해 연속 선택/해제하며 가장자리 거리에 비례해 자동 스크롤한다.
2. **iOS 근거:** `HanClip/Services/PhotoPicker.swift:772-920`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/CalendarMediaPickerSheet.kt:1538-1745`
4. **판정:** 부분
5. **보이는 차이:** Android는 최근 목록 중심이며 자동 스크롤이 16ms마다 고정 20px라 iOS와 속도·범위가 다르다.
6. **수정 파일/영역:** `CalendarMediaPickerSheet.kt` drag-select/autoscroll loop.
7. **시험:** 100개 그리드에서 상단→하단 드래그로 같은 항목 수와 스크롤 시간을 확인한다.

#### B07 — 오늘 날짜 2단계 동작
1. **iOS 실제 동작/수치:** 첫 탭은 오늘로 이동, 오늘에서 다시 탭하면 오늘 미디어를 선택한다.
2. **iOS 근거:** `HanClip/Services/PhotoPicker.swift:1653-1688`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/CalendarMediaPickerSheet.kt:1829-1837`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 과거 달에서 오늘 버튼을 두 번 눌러 이동 후 선택을 확인한다.

#### B08 — 선택 화면 회전 상태
1. **iOS 실제 동작/수치:** iPad 회전/분할 화면에서 선택을 유지하며 iPhone은 기본 세로다.
2. **iOS 근거:** `HanClip/App/HanClipApp.swift:768-780`, `HanClip/Services/PhotoPicker.swift:341-454`
3. **Android 근거:** `app/src/main/AndroidManifest.xml:90-95`, `CalendarMediaPickerSheet.kt:703-747`
4. **판정:** 부분
5. **보이는 차이:** Android는 주요 선택 상태를 `rememberSaveable`로 보존하지만 폴드 펼침·프로세스 재생성 실기기 결과는 미검증이다.
6. **수정 파일/영역:** 필요 시 `CalendarMediaPickerSheet.kt`의 saver와 window-size layout.
7. **시험:** 20개 선택 후 회전·폴드 펼침을 하고 20개와 필터·월·열 수가 유지되는지 확인한다.

#### B09 — 권한 거절
1. **iOS 실제 동작/수치:** PhotosPicker/시스템 제한 선택 UI를 사용하며 허용된 항목만 받는다.
2. **iOS 근거:** `HanClip/Services/PhotoPicker.swift:341-454`
3. **Android 근거:** `app/src/main/AndroidManifest.xml:4-9`, `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:1171-1200`
4. **판정:** 플랫폼 고유
5. **보이는 차이:** 권한 UI 모양은 다르지만 사용자가 고른 항목만 가져오는 결과는 같다. 거절 후 복구 실기기는 미검증이다.
6. **수정 파일/영역:** 없음; 실패 발견 시 `EditorRoute.kt` permission rationale.
7. **시험:** 전체 거절→제한 선택 1개→설정 허용 순서로 가져오기 결과를 확인한다.

#### B10 — 가져오기 취소와 완료분
1. **iOS 실제 동작/수치:** 작업 취소 상태·문구를 표시하고 완료되지 않은 임시 작업을 정리한다.
2. **iOS 근거:** `HanClip/ViewModels/EditorViewModel.swift:3427-3433, 4558-4573`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/media/MediaImportReader.kt:145-160`, `feature/editor/EditorViewModel.kt:330-355`
4. **판정:** 부분
5. **보이는 차이:** 양쪽에 정리는 있으나 100개 가져오기 중 취소 시 완료분·메시지의 실제 동일성은 미검증이다.
6. **수정 파일/영역:** `EditorViewModel.kt` import coroutine 결과/rollback 정책.
7. **시험:** 100개 가져오기 50%에서 취소해 완료분·임시 파일·알림을 비교한다.

### C. 편집 화면·값·상태

#### C01 — 기본시간 범위와 증감
1. **iOS 실제 동작/수치:** 0.1~30초; 0.1~1은 0.1, 1.5~10은 0.5, 11~30은 1초 단계다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:6553-6569`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/safety/DefaultDurationStepPolicy.kt:5-18`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 0.9→1.0→1.5→10→11→30 경계 값을 순서대로 확인한다.

#### C02 — 화면 비율
1. **iOS 실제 동작/수치:** 첫 사진 자동, 1:1, 3:4, 4:3, 9:16, 16:9이며 크기는 긴 변 1920 기준이다.
2. **iOS 근거:** `HanClip/Models/ClipItem.swift:188-255`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/model/OutputAspectRatio.kt:6-49`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 각 비율로 1장 영상을 만들어 해상도를 확인한다.

#### C03 — Live Photo/모션포토 상태 문구
1. **iOS 실제 동작/수치:** 선택 문구는 `사진`/`Live`다.
2. **iOS 근거:** `HanClip/Models/ClipItem.swift:257-262`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/model/ClipItem.kt:14-17`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 `사진`/`영상`으로 표시한다.
6. **수정 파일/영역:** `ClipItem.kt` `LivePhotoMode.Motion.title`과 관련 도움말.
7. **시험:** 모션포토 행의 두 선택 문구가 `사진`/`Live`인지 확인한다.

#### C04 — 모션포토 기본 재생과 보존
1. **iOS 실제 동작/수치:** Live Photo는 실제 motion duration과 paired still/video를 프로젝트에 보존한다.
2. **iOS 근거:** `HanClip/Services/ProjectStore.swift:350-390`, `HanClip/ViewModels/EditorViewModel.swift:4104-4145`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/media/MediaImportReader.kt:95-143`, `core/project/DraftProjectStore.kt:405-471`
4. **판정:** 부분
5. **보이는 차이:** Android는 추출 성공 시 두 자산을 보존하지만 일부 XMP 변형 인식과 손실 복구는 미검증이다.
6. **수정 파일/영역:** `MediaImportReader.kt` parser와 `DraftProjectStore.kt` media validation.
7. **시험:** 모션포토 저장→앱 재실행→프로젝트 열기 후 still/motion 전환을 확인한다.

#### C05 — 영상 한컷/분할
1. **iOS 실제 동작/수치:** 한컷 또는 Ai peak 기반 복수 자클립, 모클립은 유지, 자클립 제외/복원을 지원한다.
2. **iOS 근거:** `HanClip/Views/ClipRow.swift:1393-1485`, `HanClip/ViewModels/EditorViewModel.swift:1090-1118`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorViewModel.kt:1460-1615`
4. **판정:** 부분
5. **보이는 차이:** 구조와 되돌리기는 있으나 peak 결과의 동일 프레임은 실파일로 미검증이다.
6. **수정 파일/영역:** `core/media/AudioAnalysisService.kt`, `EditorViewModel.kt` segment creation.
7. **시험:** 동일 영상에서 양쪽 자클립 수와 시작/끝을 10ms 단위로 비교한다.

#### C06 — 묶음사진 자동/수동/전체
1. **iOS 실제 동작/수치:** 시간·비율·밝기·구도가 비슷한 연속 사진을 묶고 자동 N장 간격, 수동, 전체를 제공한다.
2. **iOS 근거:** `HanClip/Views/ClipRow.swift:1214-1389`, `HanClip/ViewModels/EditorViewModel.swift:1160-1260`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorViewModel.kt:1310-1460`
4. **판정:** 부분
5. **보이는 차이:** 모드와 간격은 있으나 fingerprint/대표 컷이 같은 사진을 고르는지 미검증이다.
6. **수정 파일/영역:** `MediaImportReader.kt` photo fingerprint와 `EditorViewModel.kt` grouping.
7. **시험:** 20장 연사에서 묶음 경계와 1/6 대표 사진 ID를 비교한다.

#### C07 — 클립 순서 변경
1. **iOS 실제 동작/수치:** 드래그 재정렬하며 묶음은 한 단위, 자사진은 흩어지지 않는다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:7000-7170`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:3340-3520`
4. **판정:** 부분
5. **보이는 차이:** Android도 큰 단위를 이동하지만 긴 목록 자동 스크롤·취소·접근성 이동은 미검증이다.
6. **수정 파일/영역:** `EditorRoute.kt` reorder grid gesture/semantics.
7. **시험:** 묶음 1개 포함 30개를 첫→끝으로 옮겨 내부 자사진 순서가 유지되는지 확인한다.

#### C08 — 삭제·초기화·되돌리기
1. **iOS 실제 동작/수치:** 종류별 확인 문구, 초기화 확인, 제외 후 복원 경로를 제공한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:3930-3950, 7160-7445`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:743-815`, `EditorViewModel.kt:1534-1756`
4. **판정:** 부분
5. **보이는 차이:** Android는 Snackbar형 내부 snapshot 복원을 갖지만 팝업 문구·수명·프로세스 종료 후 복원은 iOS와 같지 않다.
6. **수정 파일/영역:** `EditorDestructiveActionPolicy.kt`, `EditorViewModel.kt` undo persistence.
7. **시험:** 사진·모클립·자클립·묶음 대표를 각각 삭제 후 한 번씩 되돌린다.

#### C09 — 개별 사진 길이
1. **iOS 실제 동작/수치:** 클립 행 안에서 0.1~30초를 정확히 0.1초씩 `−/+` 조절한다.
2. **iOS 근거:** `HanClip/Views/ClipRow.swift:279-282, 1487-1549`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/PhotoDurationSheet.kt:62-190`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 별도 전체화면과 59개 slider 위치라 약 0.5초 간격이며 1.5/2/3/4/6초 프리셋을 추가했다.
6. **수정 파일/영역:** `PhotoDurationSheet.kt`를 행 내 정확한 0.1 stepper로 교체, `EditorRoute.kt` 호출부.
7. **시험:** 1.0에서 `+` 한 번 후 정확히 1.1초인지 확인한다.

#### C10 — 영상 트림 편집기
1. **iOS 실제 동작/수치:** 이전/재생/다음, 파형 양끝 drag, scrub, 반복, 삭제, 초기화/전체, 아래로 밀어 확정을 제공한다.
2. **iOS 근거:** `HanClip/Views/VideoTrimEditor.swift:74-239, 431-590, 642-1170, 1425-1438`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/VideoTrimSheet.kt:76-689`
4. **판정:** 누락
5. **보이는 차이:** Android에는 앞/뒤 클립 이동·iOS 파형 선택 gesture·반복·삭제·하향 확정이 없고 slider와 추가 프리셋만 있다.
6. **수정 파일/영역:** `VideoTrimSheet.kt` 전체 interaction model과 `EditorRoute.kt` clip navigation callback.
7. **시험:** 트림 화면에서 이전→재생→파형 drag→반복→아래 swipe 확정의 한 시나리오를 수행한다.

#### C11 — 퀵모드 길이
1. **iOS 실제 동작/수치:** 30/45/60/120/180/300초, 추천=미디어 수×1초, 최소=수×0.2초, 범위 최소~3600초, `−/+` 5초다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:15980-16299`, `HanClip/ViewModels/EditorViewModel.swift:808-827`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:1846-2211`
4. **판정:** 동일
5. **보이는 차이:** 핵심 값·문구·93 높이 확인 버튼이 같다.
6. **수정 파일/영역:** 없음.
7. **시험:** 200개 미디어에서 30초가 최소 40초로 보정되고 `+`가 45초가 되는지 확인한다.

#### C12 — 자막/워터마크 최초값
1. **iOS 실제 동작/수치:** 자막 끔, 오늘 날짜, 좌상단, Poppins, `#FFE45C`, 그림자 0.75/`#642BFF`, ExtraLarge, 저작권 우하단이다.
2. **iOS 근거:** `HanClip/Models/WatermarkSettings.swift:279-303, 662-685`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/model/WatermarkSettings.kt:119-144`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 `오늘의 스윙\nHanClip`, Pretendard, 흰색, 검정 0.2, Large 등 다른 최초값이다.
6. **수정 파일/영역:** `WatermarkSettings.kt` `projectDefault`/data class defaults.
7. **시험:** 앱 데이터 초기화 후 새 영화 자막 설정의 모든 최초값을 확인한다.

#### C13 — 자막 설정 기능
1. **iOS 실제 동작/수치:** 사용/안함, 텍스트, 날짜 삽입, 16개 서체, 색·그림자·크기·줄간격·9위치, 저작권 로고/플랫폼/사용자 아이콘을 설정한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:9000-12000`, `HanClip/Models/WatermarkSettings.swift:1-835`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/TextOverlaySheet.kt:107-1885`
4. **판정:** 부분
5. **보이는 차이:** 항목은 대부분 있으나 기본값·레이아웃·미리보기 렌더러가 달라 동일 픽셀/상태 전환은 미검증이다.
6. **수정 파일/영역:** `TextOverlaySheet.kt`, `WatermarkSettings.kt`, `VideoExportService.kt` overlay renderer.
7. **시험:** 9위치×16서체 snapshot을 iOS 결과와 픽셀 diff한다.

#### C14 — 내장 서체 파일
1. **iOS 실제 동작/수치:** 16개 자막 서체 원본을 번들한다.
2. **iOS 근거:** `HanClip/Resources/Fonts/*`
3. **Android 근거:** `app/src/main/assets/fonts/*`
4. **판정:** 동일
5. **보이는 차이:** 16쌍 SHA-256이 모두 같다.
6. **수정 파일/영역:** 없음.
7. **시험:** 빌드 산출물에서 16개를 추출해 SHA-256을 다시 비교한다.

#### C15 — 샘플 음악 목록과 원본
1. **iOS 실제 동작/수치:** 햇살 한 컷·여행의 설렘·광고 클래식 드라마·골프치러 가자·지우에게 첫눈이란·베이비 워킹 6곡이다.
2. **iOS 근거:** `HanClip/Models/ClipItem.swift:29-109`, `HanClip/Resources/Audio/*`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/model/BackgroundMusicSample.kt:6-53`, `app/src/main/res/raw/*`
4. **판정:** 잘못 구현
5. **보이는 차이:** 목록은 같지만 `travel_joy.wav`와 `golf_lets_go.wav` SHA-256이 iOS 원본과 다르므로 실제 음악 결과가 다르다.
6. **수정 파일/영역:** `app/src/main/res/raw/travel_joy.wav`, `golf_lets_go.wav`를 iOS 기준 원본으로 교체.
7. **시험:** 6곡 파일 SHA-256이 모두 iOS 자산과 같은지 확인한다.

#### C16 — 음악 음량
1. **iOS 실제 동작/수치:** 배경음·원본음 0~100% 연속 slider다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:13284-13305`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/MusicSettingsSheet.kt:391-423`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android `steps=19`로 5% 단위다.
6. **수정 파일/영역:** `MusicSettingsSheet.kt` 두 Slider의 `steps` 제거.
7. **시험:** 배경음량을 53%로 설정·저장·재열기해 53%가 유지되는지 확인한다.

#### C17 — 음악 설정 저장/취소
1. **iOS 실제 동작/수치:** 진입 snapshot을 갖고 변경 후 X에서 `저장 없이 나가기`를 고르면 원래 값으로 복원, 디스크 아이콘으로 저장한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:12791-12889, 13208-13230`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:1087-1120`, `MusicSettingsSheet.kt:73-350`
4. **판정:** 누락
5. **보이는 차이:** Android는 조절 즉시 ViewModel을 바꾸고 닫기에서 복원/저장 확인이 없다.
6. **수정 파일/영역:** `EditorRoute.kt` music draft state와 `MusicSettingsSheet.kt` close/save toolbar.
7. **시험:** 20%→80% 변경 후 저장 없이 닫아 다시 20%인지 확인한다.

#### C18 — 음악 반복·페이드
1. **iOS 실제 동작/수치:** 영상 채우기 반복, fade-in 0.3초, fade-out 1.0초 설정을 저장·합성한다.
2. **iOS 근거:** `HanClip/Models/ClipItem.swift:29-109`, `HanClip/Services/VideoComposer.swift:674-790`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/media/VideoExportService.kt:178-188, 454-476`
4. **판정:** 동일
5. **보이는 차이:** 핵심 합성 시간은 같다.
6. **수정 파일/영역:** 없음.
7. **시험:** 10초 음악/30초 영상에서 반복과 양끝 fade 시간을 파형으로 확인한다.

#### C19 — 엔딩 테마와 값
1. **iOS 실제 동작/수치:** 자막·보물지도·여행일정·랜드마크·오피스 5종, 기본 안함, 1~10초, 0.5초 step이다.
2. **iOS 근거:** `HanClip/Models/WatermarkSettings.swift:70-120, 831-833`, `HanClip/Views/EditorView.swift:12148-12175`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/model/WatermarkSettings.kt:77-83, 166-167`, `EndingInfoSettingsSheet.kt:49-340`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android 정규화는 `(value*2).toInt()/2`라 1.76초를 1.5초로 내림하지만 iOS는 2.0초로 반올림한다.
6. **수정 파일/영역:** `WatermarkSettings.kt` `normalizedEndingInfoCardDuration`을 round 기반으로 변경.
7. **시험:** 저장 데이터에 1.76초를 넣고 설정을 열어 2.0초인지 확인한다.

#### C20 — 엔딩 위치·경로·미리보기
1. **iOS 실제 동작/수치:** 촬영 날짜/도시로 stop을 만들고 국내 도시/해외 국가, 차량/비행기, 테마별 실제 비율 미리보기를 만든다.
2. **iOS 근거:** `HanClip/ViewModels/EditorViewModel.swift:1820-1910`, `HanClip/Views/EditorView.swift:12018-12750`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/TextOverlaySheet.kt:709-1090`, `EditorViewModel.kt:2437-2478`
4. **판정:** 부분
5. **보이는 차이:** 기능 구조는 있으나 geocoder 도시명·그림문자·경로 seed·픽셀 렌더가 동일한지 미검증이다.
6. **수정 파일/영역:** `TextOverlaySheet.kt` preview와 `core/media/EndingInfoCardRenderer.kt`.
7. **시험:** 서울→부산→도쿄 3일 샘플에서 문구·아이콘·줄바꿈·최종 프레임을 비교한다.

### D. 내보내기·시사회·플레이어

#### D01 — 프레임레이트와 품질 선택
1. **iOS 실제 동작/수치:** 모든 결과는 30fps이며 별도 품질 선택이 없다.
2. **iOS 근거:** `HanClip/Services/VideoComposer.swift:82-93, 297-303`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/model/OutputQualityPreset.kt:3-22`, `feature/editor/EditorRoute.kt:1569-1581`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 표준30/부드러운60을 노출하고 60fps 결과를 만들 수 있다.
6. **수정 파일/영역:** `OutputQualityPreset.kt`, `EditorRoute.kt` 품질 UI, `VideoExportService.kt` frame rate.
7. **시험:** 새 영화 내보내기 결과의 nominal frame rate가 항상 30인지 확인한다.

#### D02 — 사진 장면 애니메이션
1. **iOS 실제 동작/수치:** 사진마다 30fps still movie를 만들고 랜덤 초점의 zoom-in/out transform을 적용한다.
2. **iOS 근거:** `HanClip/Services/VideoComposer.swift:334, 1131-1240`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/media/VideoExportService.kt:407-440`
4. **판정:** 누락
5. **보이는 차이:** Android 사진은 정적인 crop으로 유지된다.
6. **수정 파일/영역:** `VideoExportService.kt` photo `Presentation` effect에 iOS와 같은 seeded zoom transform 추가.
7. **시험:** 사진 3장 결과에서 각 장의 첫/끝 transform이 움직이는지 확인한다.

#### D03 — 화면 비율 aspect-fill
1. **iOS 실제 동작/수치:** 모든 소스를 선택 출력 비율에 중앙 aspect-fill 한다.
2. **iOS 근거:** `HanClip/Services/VideoComposer.swift:267-290`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/media/VideoExportService.kt:407-440`
4. **판정:** 동일
5. **보이는 차이:** 수학식 결과는 같다.
6. **수정 파일/영역:** 없음.
7. **시험:** 4:3 소스를 9:16으로 내보내 중앙 crop 좌표를 비교한다.

#### D04 — 원본 영상·모션포토 오디오
1. **iOS 실제 동작/수치:** 원본에 audio track이 있으면 영상과 Live Photo motion 구간에 삽입한다.
2. **iOS 근거:** `HanClip/Services/VideoComposer.swift:214-225`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/media/VideoExportService.kt:165-170`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 `mediaKind != Video`면 오디오를 제거해 모션포토의 소리를 잃는다.
6. **수정 파일/영역:** `VideoExportService.kt` `setRemoveAudio` 조건을 실제 motion audio track 기준으로 변경.
7. **시험:** 소리 있는 모션포토 하나를 영상 모드로 내보내 원본 소리가 남는지 확인한다.

#### D05 — 엔딩 구간 워터마크
1. **iOS 실제 동작/수치:** 엔딩 카드가 시작되면 일반 자막 overlay만 중단하고 저작권 로고 overlay는 끝까지 유지한다.
2. **iOS 근거:** `HanClip/Services/VideoComposer.swift:895-917`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/media/VideoExportService.kt:131-172, 407-440`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 엔딩도 같은 composition item effect를 받아 일반 자막이 엔딩 위에 겹치며, 로고는 iOS와 동일하게 유지돼야 한다.
6. **수정 파일/영역:** `VideoExportService.kt` ending item에서 자막 overlay만 제외하는 분기.
7. **시험:** 자막+로고+엔딩을 켜고 마지막 카드에서 자막은 없고 로고는 남는지 확인한다.

#### D06 — 생성 진행·취소·복구
1. **iOS 실제 동작/수치:** 썸네일·진행바·진행률·취소를 표시하고 취소 중 문구 후 임시 결과를 정리한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:7440-7605`, `HanClip/ViewModels/EditorViewModel.swift:1800-1810`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:650-730`, `core/media/VideoExportService.kt:205-248`
4. **판정:** 부분
5. **보이는 차이:** 진행/취소/정리는 있으나 foreground notification·앱 종료 후 상태 수명이 iOS와 다르고 실기기 취소 복구는 미검증이다.
6. **수정 파일/영역:** `ExportForegroundService.kt`, `EditorViewModel.kt` export lifecycle.
7. **시험:** 10분 영상을 50%에서 취소하고 앱 재실행 후 임시 MP4와 진행 상태가 없는지 확인한다.

#### D07 — 저장 공간 부족
1. **iOS 실제 동작/수치:** 쓰기/저장 실패를 사용자 오류로 보고 원본 프로젝트를 유지한다.
2. **iOS 근거:** `HanClip/Services/ProjectStore.swift:760-770, 931-940`, `VideoComposer.swift:320-350`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/safety/StorageSpaceGuard.kt:1-90`, `VideoSaveShare.kt:19-70`
4. **판정:** 부분
5. **보이는 차이:** Android에 사전 검사와 transaction 정리가 있으나 실제 0-byte/ENOSPC에서 문구·원본 보존은 미검증이다.
6. **수정 파일/영역:** `StorageSpaceGuard.kt`, `VideoExportService.kt`, `VideoSaveShare.kt` error mapping.
7. **시험:** quota가 10MB 남은 환경에서 100MB 내보내기를 시도해 원본과 프로젝트가 유지되는지 확인한다.

#### D08 — 사진 앱/파일 앱 저장 UI
1. **iOS 실제 동작/수치:** 시사회 `개봉하기`에서 사진 앱 앨범명 또는 파일 앱 저장을 고른다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:16454-16930`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:732-1030`, `core/media/VideoSaveShare.kt:19-70`
4. **판정:** 플랫폼 고유
5. **보이는 차이:** 시스템 picker/MediaStore UI는 다르지만 사진 보관함 또는 사용자 지정 파일로 남기는 결과는 같다. 권한 거절은 미검증이다.
6. **수정 파일/영역:** 없음.
7. **시험:** 두 저장 목적지에 같은 제목으로 저장하고 앱 재실행 후 파일 존재를 확인한다.

#### D09 — 전체화면 단일 탭·자동 숨김·반복
1. **iOS 실제 동작/수치:** 단일 탭 재생/정지, 컨트롤 3초 후 숨김, 끝에서 반복한다.
2. **iOS 근거:** `HanClip/Views/HanClipFullscreenVideoPlayer.swift:701-748, 868-882`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:1065-1389`
4. **판정:** 동일
5. **보이는 차이:** 핵심 동작과 3초 값이 같다.
6. **수정 파일/영역:** 없음.
7. **시험:** 단일 탭 후 3초에 컨트롤이 사라지고 끝에서 0초로 재생되는지 확인한다.

#### D10 — 전체화면 더블 탭
1. **iOS 실제 동작/수치:** 확대율이 1이 아니면 1로 복원하고, 이미 1이면 아무 동작도 하지 않는다.
2. **iOS 근거:** `HanClip/Views/HanClipFullscreenVideoPlayer.swift:868-879`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:1255-1269`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android 1배 더블 탭은 좌/우에 따라 10초 탐색한다.
6. **수정 파일/영역:** `PreviewRoute.kt` doubleTap handler.
7. **시험:** 1배 화면 좌우를 더블 탭해 재생 시간이 변하지 않는지 확인한다.

#### D11 — 전체화면 가로 scrub
1. **iOS 실제 동작/수치:** 영상 면 전체의 가로 drag로 재생 위치를 찾는다.
2. **iOS 근거:** `HanClip/Views/HanClipFullscreenVideoPlayer.swift:945-1024`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:1198-1275`
4. **판정:** 누락
5. **보이는 차이:** Android는 controller slider 외 영상 면 가로 scrub이 없다.
6. **수정 파일/영역:** `PreviewRoute.kt` full-surface gesture state machine.
7. **시험:** 컨트롤을 숨긴 상태에서 화면 중앙을 가로로 밀어 시간이 변하는지 확인한다.

#### D12 — 전체화면 위로 밀어 음량
1. **iOS 실제 동작/수치:** 위로 시작한 drag는 손을 뗄 때까지 시스템 음량 모드로 고정되고 아래로 되돌리면 낮아진다.
2. **iOS 근거:** `HanClip/Views/HanClipFullscreenVideoPlayer.swift:945-1024`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:1198-1213`
4. **판정:** 누락
5. **보이는 차이:** Android에는 위 swipe 음량 조절이 없다.
6. **수정 파일/영역:** `PreviewRoute.kt` vertical gesture와 AudioManager volume bridge.
7. **시험:** 화면 중앙을 위→아래로 한 번 이어 밀어 시스템 음량이 증가 후 감소하는지 확인한다.

#### D13 — 아래로 밀어 닫기
1. **iOS 실제 동작/수치:** 닫기 threshold는 `max(55pt, 화면높이×0.08)`이다.
2. **iOS 근거:** `HanClip/Views/HanClipFullscreenVideoPlayer.swift:1027-1048`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:1094, 1198-1213`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 고정 120dp라 같은 손 이동에서 닫힘 여부가 다르다.
6. **수정 파일/영역:** `PreviewRoute.kt` dismiss threshold 계산.
7. **시험:** 높이 800dp에서 64dp 아래 swipe로 닫히는지 확인한다.

#### D14 — 핀치 확대
1. **iOS 실제 동작/수치:** 0.5~4배, 확대 상태 pan, 더블 탭 1배다.
2. **iOS 근거:** `HanClip/Views/HanClipFullscreenVideoPlayer.swift:789-831, 945-1024`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:1230-1269`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android 최소는 1배라 0.5배 축소가 불가능하다.
6. **수정 파일/영역:** `PreviewRoute.kt` scale clamp와 offset bounds.
7. **시험:** pinch-in으로 표시 배율이 0.5까지 내려가는지 확인한다.

#### D15 — 플레이어 회전·가로 맞춤/채우기
1. **iOS 실제 동작/수치:** iPhone은 플레이어에서 실제 기기 방향으로 앱 orientation을 전환하고 가로일 때 맞춤/채우기를 노출한다.
2. **iOS 근거:** `HanClip/App/HanClipApp.swift:768-780`, `HanClip/Views/HanClipFullscreenVideoPlayer.swift:789-831`
3. **Android 근거:** `app/src/main/AndroidManifest.xml:90-95`, `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:1065-1389`
4. **판정:** 부분
5. **보이는 차이:** 소스상 orientation/config 처리와 aspect toggle은 있으나 안전영역·폴드·태블릿 실제 결과는 미검증이다.
6. **수정 파일/영역:** `PreviewRoute.kt` window metrics/system bars와 `MainActivity.kt` orientation policy.
7. **시험:** 세로 재생 중 기기를 가로로 돌려 맞춤/채우기 노출과 뒤로가기 복원을 확인한다.

### E. AiShot·브라우저·컬렉션

#### E01 — AiShot 감도·샷 길이
1. **iOS 실제 동작/수치:** 감도 시끄러움/일반/조용함/자동(기본 자동), 짧게 1.5+1.5, 일반 2+3, 길게 5+5초다.
2. **iOS 근거:** `HanClip/Services/AiShotCamera.swift:6-117, 127-163`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/aishot/AiShotRoute.kt:116-218`
4. **판정:** 동일
5. **보이는 차이:** 없음. Android 표시 행도 `Loud→Normal→Quiet→Auto`로 별도 구성한다(`AiShotRoute.kt:823-826`).
6. **수정 파일/영역:** 없음; 표시 순서가 다르면 `AiShotRoute.kt` chip list.
7. **시험:** 일반 샷에서 트리거 전 2초·후 3초 결과를 확인한다.

#### E02 — AiShot 줌
1. **iOS 실제 동작/수치:** 기기 지원 lens factor를 동적으로 나열하고 logarithmic drag/dial로 정밀 zoom을 제공한다.
2. **iOS 근거:** `HanClip/Services/AiShotCamera.swift:477-541, 1646-1845, 1937-1941`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/aishot/AiShotRoute.kt`의 기기 zoom 범위, lens factor 버튼, 로그 정밀 조절.
4. **판정:** 부분
5. **보이는 차이:** 로그 배율과 0.5초 자동 닫힘은 같지만 물리 렌즈 전환 결과는 플랫폼 카메라 장치에 따라 달라 실기기 비교가 남았다.
6. **수정 파일/영역:** 소스 반영 완료. SM-F968N과 기준 iPhone의 물리 렌즈 전환 비교만 남음.
7. **시험:** 1.0에서 drag로 1.3배를 선택하고 촬영 중 유지되는지 확인한다.

#### E03 — AiShot 촬영 결과 규격
1. **iOS 실제 동작/수치:** 1080×1440, 30fps, aspect-fill MOV highest quality로 trim한다.
2. **iOS 근거:** `HanClip/Services/AiShotCamera.swift:1909-1911, 2851-2937`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/aishot/AiShotRoute.kt:480-483`, `AiShotVideoTrimmer.kt:36-110`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android CameraX는 `Quality.HD`(720p 계열)이고 trim은 H264/AAC만 지정해 1080×1440/30 보장을 하지 않는다.
6. **수정 파일/영역:** `AiShotRoute.kt` Recorder quality/target aspect, `AiShotVideoTrimmer.kt` scale/crop/frame-rate effect.
7. **시험:** 전·후면 각 1개 결과가 정확히 1080×1440/30fps인지 검사한다.

#### E04 — AiShot 권한·중단·실패
1. **iOS 실제 동작/수치:** 카메라/마이크 권한, background interruption 시 촬영 cover 재시작, 실패 status를 관리한다.
2. **iOS 근거:** `HanClip/Services/AiShotCamera.swift:1906-1975`, `HanClip/Views/EditorView.swift:1125-1139`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/aishot/AiShotRoute.kt:471-545, 1242-1274`
4. **판정:** 부분
5. **보이는 차이:** Android는 설정 버튼을 추가했지만 통화/백그라운드/권한 철회 뒤 rolling buffer 복구 동일성은 미검증이다.
6. **수정 파일/영역:** `AiShotRoute.kt` lifecycle observer/error recovery.
7. **시험:** 촬영 중 홈→복귀와 권한 철회→허용을 거쳐 자동 감지가 다시 시작되는지 확인한다.

#### E05 — 브라우저 기본 사이트
1. **iOS 실제 동작/수치:** Pixabay Music·Mixkit Music·YouTube 3개 기본 즐겨찾기다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:13384-13409`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/browser/OnlineMusicBrowserRoute.kt:1212-1216`
4. **판정:** 동일
5. **보이는 차이:** 없음.
6. **수정 파일/영역:** 없음.
7. **시험:** 데이터 초기화 후 즐겨찾기에 세 주소가 순서대로 있는지 확인한다.

#### E06 — 감지 영상 패널
1. **iOS 실제 동작/수치:** 영상 감지 시 `다운`·`보기`·`닫기` 세 버튼을 제공한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:13952-13999`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/browser/OnlineMusicBrowserRoute.kt:912-940`
4. **판정:** 누락
5. **보이는 차이:** Android는 `받기`·`닫기`만 있고 다운로드 전 전체화면 `보기`가 없다.
6. **수정 파일/영역:** `OnlineMusicBrowserRoute.kt` detected-video panel과 Preview player route.
7. **시험:** 직접 MP4 페이지에서 감지 후 `보기`로 저장 없이 재생되는지 확인한다.

#### E07 — 즐겨찾기 가져오기 중복
1. **iOS 실제 동작/수치:** 같은 주소는 가져온 파일 값으로 덮어쓰고 새 주소는 추가한다.
2. **iOS 근거:** `HanClip/ViewModels/EditorViewModel.swift:3782-3844`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/browser/OnlineMusicBrowserRoute.kt:1238-1269`
4. **판정:** 잘못 구현
5. **보이는 차이:** Android는 중복 주소를 제외해 가져온 제목/홈 지정이 반영되지 않는다.
6. **수정 파일/영역:** `OnlineMusicBrowserRoute.kt` `BrowserFavoritesStore.mergeImported`.
7. **시험:** 같은 URL의 제목이 다른 파일을 가져와 새 제목으로 바뀌는지 확인한다.

#### E08 — 브라우저 다운로드·취소
1. **iOS 실제 동작/수치:** 다운로드 진행률·취소 상태를 브라우저에 표시하고 완료 후 파일로 가져온다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:14030, 15038-15045`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/browser/OnlineMusicBrowserRoute.kt:744-990`
4. **판정:** 부분
5. **보이는 차이:** 진행/취소는 있으나 background·손상 응답·재시작 후 임시 파일 정리는 미검증이다.
6. **수정 파일/영역:** `OnlineMusicBrowserRoute.kt` download transaction/recovery.
7. **시험:** 1GB 직접 URL을 30%에서 취소해 임시 파일과 편집 항목이 남지 않는지 확인한다.

#### E09 — 컬렉션 한도·압축 값
1. **iOS 실제 동작/수치:** 최대 30개; 1080p 8.5Mbps, 720p 5Mbps, 540p 2.5Mbps; 더 작아진 경우만 교체한다.
2. **iOS 근거:** `HanClip/Services/MovieCollectionStore.swift:35-81, 99-114, 138-141, 197-282`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/project/MovieCollectionStore.kt:97-169, 250-350`
4. **판정:** 동일
5. **보이는 차이:** 수치·예상식·작을 때만 교체 규칙이 같다.
6. **수정 파일/영역:** 없음.
7. **시험:** 4K 영상에 세 옵션을 적용해 해상도·bitrate 상한과 파일 감소를 확인한다.

#### E10 — 컬렉션 포스터 AI 후보
1. **iOS 실제 동작/수치:** device AI 8개+HanClip AI 8개, Vision 얼굴/주목/feature-print와 별도 점수로 후보를 만든다.
2. **iOS 근거:** `HanClip/Services/MovieCollectionStore.swift:117-135, 584-741`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/project/MovieCollectionStore.kt:85-95, 380-430`
4. **판정:** 부분
5. **보이는 차이:** Android도 8+8이지만 Android bitmap 기반 독립 점수라 같은 프레임을 고른다는 근거가 없다.
6. **수정 파일/영역:** `MovieCollectionStore.kt` poster sampling/scoring/diversity exclusion.
7. **시험:** 기준 영상 20개에서 양쪽 16개 후보 timestamp와 1순위를 비교한다.

#### E11 — 컬렉션 핀·순서·제목·공유·제거
1. **iOS 실제 동작/수치:** hole 탭 pin, pinned poster drag 재정렬, long-press 메뉴에서 제목·AI·압축·공유·제거를 제공한다.
2. **iOS 근거:** `HanClip/Services/MovieCollectionStore.swift:951-993`, `HanClip/Views/EditorView.swift:3900-4520`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/home/MovieCollectionSection.kt:538-910`, `core/project/MovieCollectionStore.kt:693-725`
4. **판정:** 부분
5. **보이는 차이:** Android는 pinned 순서를 메뉴의 앞/뒤 이동으로 처리해 iOS drag-onto 동작과 다르며 실제 long-press 메뉴 배열도 다르다.
6. **수정 파일/영역:** `MovieCollectionSection.kt` pinned drag/drop와 action menu.
7. **시험:** 핀 3개 중 3번째를 1번째 위로 끌어 순서가 3-1-2가 되는지 확인한다.

#### E12 — 컬렉션 가져오기 취소·중복·완료분
1. **iOS 실제 동작/수치:** 영상만, 진행률/완료 수, 취소 시 완료된 영상 유지, 중복/실패 보고다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:4500-4665`, `HanClip/Services/MovieCollectionStore.swift:302-350`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/home/HomeRoute.kt:352-406`
4. **판정:** 동일
5. **보이는 차이:** 소스상 결과 수명과 문구 의미가 같다.
6. **수정 파일/영역:** 없음.
7. **시험:** 10개 중 5번째에서 취소해 완료된 4개만 남는지 확인한다.

### F. 접근성·적응형 화면·오류·보존

#### F01 — 접근성 이름과 선택 상태
1. **iOS 실제 동작/수치:** 주요 헤더·취소·플레이어에 label/hint/value와 재생 ±10초 accessibility action을 제공한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:1368, 2377-2379`, `HanClip/Views/HanClipFullscreenVideoPlayer.swift:406-419`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/preview/PreviewRoute.kt:1065-1440`, `feature/editor/EditorRoute.kt:1248-1450`
4. **판정:** 부분
5. **보이는 차이:** Android contentDescription/selectable은 있으나 플레이어 ±10초 custom accessibility action과 전체 selected/stateDescription 동등성은 없다.
6. **수정 파일/영역:** `PreviewRoute.kt` semantics customActions, 각 segmented control `stateDescription`.
7. **시험:** TalkBack만으로 재생 위치 ±10초와 선택 상태를 읽고 실행한다.

#### F02 — 터치 영역
1. **iOS 실제 동작/수치:** 주요 헤더·stepper는 44pt 이상, 퀵 `−/+`는 72×51pt다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:16166-16200`, `HanClip/Views/ClipRow.swift:1487-1549`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:1945-1999`, `HomeRoute.kt:3313-3322`
4. **판정:** 부분
5. **보이는 차이:** 주요 버튼은 48/51/52dp지만 작은 collection hole·아이콘의 실제 semantics bounds는 미검증이다.
6. **수정 파일/영역:** `MovieCollectionSection.kt` pin hole과 모든 icon-only clickable 최소 48dp.
7. **시험:** Accessibility Scanner에서 모든 actionable node가 48×48dp 이상인지 확인한다.

#### F03 — 글자 확대
1. **iOS 실제 동작/수치:** 일부 SwiftUI text는 고정 `.system(size:)`, 일부 UIKit label은 명시적 Dynamic Type 설정이 없어 전체 확대가 완전하지 않다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:16021-16023`, `HanClipShare/ShareViewController.swift:53-84`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:1909-1913`, `HomeRoute.kt:1214-1360`
4. **판정:** 부분
5. **보이는 차이:** Android `sp`는 글꼴 배율을 따르지만 고정 높이/한 줄 제한에서 잘릴 수 있다. 최대 글자 크기 실기기 미검증이다.
6. **수정 파일/영역:** 주요 화면 고정 height/maxLines를 `EditorRoute.kt`, `HomeRoute.kt`, `PreviewRoute.kt`에서 adaptive하게 변경.
7. **시험:** 글자 200%에서 모든 버튼 문구가 잘리지 않고 기능이 노출되는지 확인한다.

#### F04 — 좁은 화면·태블릿·폴드
1. **iOS 실제 동작/수치:** iPad 세로/가로·분할을 지원하고 콘텐츠 폭을 제한하며 share extension도 pad layout을 분기한다.
2. **iOS 근거:** `HanClip/App/HanClipApp.swift:768-780`, `HanClipShare/ShareViewController.swift:43-45, 171-180`
3. **Android 근거:** `app/src/main/AndroidManifest.xml:90-95`, `HomeRoute.kt:215, 426-433`, `EditorRoute.kt:186, 406-410`
4. **판정:** 부분
5. **보이는 차이:** Android는 600dp에서 편집 2열, 홈 max 920dp지만 폴드 hinge 회피·펼침 중 dialog/gesture 보존은 구현 근거가 부족하고 미검증이다.
6. **수정 파일/영역:** window-size/hinge 정보를 `HomeRoute.kt`, `EditorRoute.kt`, 모든 full-screen dialog에 전달.
7. **시험:** 접힌 360dp→펼친 720dp 전환에서 선택·dialog·스크롤 위치를 확인한다.

#### F05 — 세로·가로 회전 후 편집 상태
1. **iOS 실제 동작/수치:** iPhone 편집은 세로 고정, iPad는 모두 허용하며 ObservableObject 상태를 유지한다.
2. **iOS 근거:** `HanClip/App/HanClipApp.swift:768-780`
3. **Android 근거:** `app/src/main/AndroidManifest.xml:90-95`, `feature/editor/EditorRoute.kt:171-410`
4. **판정:** 부분
5. **보이는 차이:** Android는 편집도 자유 회전하며 `configChanges`로 Activity를 유지한다. 플랫폼 차이를 넘어 iPhone 기준 상호작용과 다르고 실제 state 보존은 미검증이다.
6. **수정 파일/영역:** `MainActivity.kt` destination별 orientation policy 또는 감사 승인된 Android 적응 정책 명시.
7. **시험:** 편집 중 음악 dialog·트림·선택 상태를 둔 채 4방향 회전해 값 손실을 확인한다.

#### F06 — 손상된 프로젝트 메타데이터
1. **iOS 실제 동작/수치:** 읽지 못하는 프로젝트는 목록에서 제외하며 저장 공간 오류를 사용자에게 보고한다.
2. **iOS 근거:** `HanClip/Services/ProjectStore.swift:44-59, 931-940`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/project/DraftProjectStore.kt:332-357, 609-645`
4. **판정:** 부분
5. **보이는 차이:** Android는 primary/backup atomic JSON 복구가 있지만 둘 다 손상되면 조용히 제외되어 사용자 오류 안내가 없다.
6. **수정 파일/영역:** `DraftProjectStore.kt` load recovery result와 `HomeRoute.kt` recovery banner.
7. **시험:** `project.json`과 `.bak`를 모두 손상시켜 사용자에게 복구 불가 항목 수가 보이는지 확인한다.

#### F07 — 손상된 미디어와 모션 fallback
1. **iOS 실제 동작/수치:** 불러오기 실패를 alert로 보고 해당 미디어를 추가하지 않으며 기존 프로젝트는 유지한다.
2. **iOS 근거:** `HanClip/ViewModels/EditorViewModel.swift:4040-4170, 4558-4573`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/media/MediaImportReader.kt:73-149`, `feature/editor/EditorViewModel.kt:990-1042`
4. **판정:** 부분
5. **보이는 차이:** Android는 motion video 유실을 still로 fallback하지만 일반 손상 파일별 사용자 메시지와 batch 완료 수가 iOS와 정확히 같지 않다.
6. **수정 파일/영역:** `MediaImportReader.kt` typed error, `EditorViewModel.kt` batch error aggregation.
7. **시험:** 정상 사진+손상 MP4+motion video 유실 프로젝트를 열어 정상 항목 보존과 오류 문구를 확인한다.

#### F08 — 사용자 파일 보존과 원자적 교체
1. **iOS 실제 동작/수치:** 프로젝트·컬렉션에 사용자 미디어를 복사하고 압축 성공 뒤에만 원본을 교체한다.
2. **iOS 근거:** `HanClip/Services/ProjectStore.swift:90-180, 760-930`, `MovieCollectionStore.swift:197-282`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/core/project/DraftProjectStore.kt:378-645`, `MovieCollectionStore.kt:430-510, 557-650`
4. **판정:** 동일
5. **보이는 차이:** staging/backup/검증 후 교체 구조가 있고 외부 원본 URI를 삭제하지 않는다.
6. **수정 파일/영역:** 없음.
7. **시험:** 저장/압축 중 프로세스를 종료해 원본 또는 검증된 backup으로 복구되는지 확인한다.

#### F09 — 뒤로가기
1. **iOS 실제 동작/수치:** cover/sheet는 닫고 편집 로고 홈은 저장 선택 팝업을 거치며 플레이어는 dismiss한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:703-1076, 2320-2598`, `HanClipFullscreenVideoPlayer.swift:294-420`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:388-399, 817-875`, `PreviewRoute.kt:1065-1400`
4. **판정:** 부분
5. **보이는 차이:** Android 시스템 Back은 편집에서 확인창을 열지만 nested dialog/브라우저/플레이어의 우선순위와 predictive back은 미검증이다.
6. **수정 파일/영역:** 각 Route의 `BackHandler`를 중앙 navigation state machine으로 정리.
7. **시험:** 각 전체화면/팝업/메뉴에서 Back을 한 번씩 눌러 바로 아래 계층만 닫히는지 확인한다.

#### F10 — 색·크기·간격·모서리·테두리·정렬·애니메이션
1. **iOS 실제 동작/수치:** 화면별 SwiftUI 상수(예: 퀵 버튼 93pt, 14pt panel radius, 0.8/1pt divider, 0.11~0.20초 transition)를 사용한다.
2. **iOS 근거:** `HanClip/Views/EditorView.swift:347-499, 1236, 16044-16111`
3. **Android 근거:** `app/src/main/java/com/hanclip/android/feature/editor/EditorRoute.kt:1846-2158`, `feature/home/HomeRoute.kt:420-590`
4. **판정:** 부분
5. **보이는 차이:** 일부 수치는 복제됐지만 테마부터 다르고 Material 기본 ripple/dialog/typography가 섞여 전체 픽셀·animation curve 동등성 근거가 없다. 스크린샷 검증 미수행이다.
6. **수정 파일/영역:** 테마 수정 후 `HomeRoute.kt`, `EditorRoute.kt`, 각 Sheet/Preview의 shared token과 animation spec.
7. **시험:** 360×800, 600×960, 1280×800 세 크기 golden screenshot을 픽셀 diff한다.

## 정확한 집계

아래 수치는 이 문서의 `####` 감사 항목만 집계하며, 요약/목록은 중복 계산하지 않는다.

- 전체 항목 수: **81**
- 동일: **22**
- 부분: **31**
- 누락: **8**
- 잘못 구현: **18**
- 플랫폼 고유: **2**

현재 결론은 **Android가 iOS와 완전히 동일하지 않다**이다. 특히 구매/복원, 트림 편집기, 플레이어 surface gesture, 사진 애니메이션, 자동 저장 수명, 모션포토 오디오, AiShot 출력 규격은 사용자 결과 또는 데이터 수명을 바꾼다.

## 남은 차이 — 심각도순 완전 목록

### S0 — 결과·결제·데이터 수명

1. A14 구매/복원/entitlement 전체 누락.
2. A11 미저장 편집을 Android가 자동 저장해 강제 종료 후 상태가 달라짐.
3. D04 모션포토 원본 오디오 제거.
4. D05 엔딩 카드 위 일반 자막 중첩 가능(로고는 iOS도 유지).
5. E03 AiShot 결과가 1080×1440/30fps로 고정되지 않음.
6. D02 사진 zoom 애니메이션 누락.
7. C15 여행·골프 샘플 음악 원본 불일치.

### S1 — 핵심 기능·상호작용

1. C10 iOS 트림 편집기의 이전/다음·파형 gesture·loop·삭제·하향 확정 누락.
2. D11 영상 면 가로 scrub 누락.
3. D12 위 swipe 시스템 음량 누락.
4. E02 AiShot 정밀 zoom/dynamic lens 누락.
5. E06 브라우저 감지 영상 `보기` 누락.
6. C17 음악 `저장 없이 나가기` 복원 누락.
7. D01 Android 60fps 추가로 결과 규격 불일치.
8. D10 더블 탭 10초 탐색 오동작.
9. D13 닫기 threshold 불일치.
10. D14 최소 zoom 1배로 잘못 제한.
11. C09 사진 길이 UI/step 불일치.
12. C12 워터마크 최초값 불일치.
13. C16 음량 5% step 불일치.
14. C19 엔딩 시간 내림 정규화.
15. A07 AiShot·여행 로고 preset 불일치.
16. A09 메모 80자 절단.
17. A12 자동 테마 색 불일치.
18. C03 `Live` 문구가 `영상`으로 다름.
19. E07 즐겨찾기 중복 덮어쓰기 정책 불일치.

### S2 — 부분 구현·복구·적응형 UI

A04, A05, A13, B01, B05, B06, B08, B10, C04, C05, C06, C07, C08, C13, C20, D06, D07, D15, E04, E08, E10, E11, F01, F02, F03, F04, F05, F06, F07, F09, F10. 각 항목의 사용자 차이와 정확한 수정 영역은 본문에 적었다.

### S3 — 허용 가능한 시스템 UI 차이

B09 권한 시스템 UI, D08 사진/파일 저장 시스템 UI. 둘 다 사용자 결과·상태 수명 실기기 시험은 남아 있다.

## Android 개발자가 그대로 실행할 수정 순서

1. `CopyrightPurchaseManager.kt`와 Play Billing UI/복원/entitlement를 구현하고 A14 시험을 먼저 고정한다.
2. `EditorRoute.kt`의 즉시 autosave를 session journal로 분리해 명시적 저장만 commit하고 A10/A11 회귀시험을 만든다.
3. `VideoExportService.kt`에서 모션포토 audio 보존, 엔딩 overlay 제외, 30fps 고정, 사진 zoom transform을 순서대로 수정한다.
4. iOS 원본 `HanClipTravelJoy.wav`, `HanClipGolfLetsGo.wav`와 Android 자산 hash를 맞춘다.
5. `AiShotRoute.kt`/`AiShotVideoTrimmer.kt`를 1080×1440/30fps와 dynamic/precision zoom에 맞춘다.
6. `PreviewRoute.kt` gesture를 iOS state machine(가로 scrub/위 음량/아래 닫기/0.5~4배/double-tap reset)으로 교체한다.
7. `VideoTrimSheet.kt`에 이전·다음, waveform handle, scrub/loop/delete/reset/down-confirm을 추가한다.
8. `WatermarkSettings.kt`의 최초값·preset logo·ending round를 수정한 뒤 text/ending renderer snapshot을 맞춘다.
9. `MusicSettingsSheet.kt`를 continuous slider와 transactional save/cancel로 바꾼다.
10. `PhotoDurationSheet.kt` 대신 0.1초 행 내 stepper를 구현한다.
11. 브라우저 `보기`와 imported favorite overwrite를 구현한다.
12. widget 일반 open을 보완한다. 앱 아이콘 shortcut 3종은 변경하지 않는다.
13. 테마 토큰을 iOS 자동 팔레트로 맞추고 Material 기본 component까지 같은 토큰을 사용하게 한다.
14. picker long-press/drag, collection pinned drag, accessibility custom action, fold/large-text layout을 보완한다.
15. 손상 파일·ENOSPC·중단 복구 instrumentation test와 세 화면 크기 golden screenshot을 수행한다.

## iOS 모든 진입점 역대조

| iOS 호출 가능 진입점 | iOS 근거 | Android 대응 | 감사 ID |
|---|---|---|---|
| 홈/편집 root | `EditorView.swift:287-313` | `HanClipApp.kt:241-369` | A01 |
| 사진/달력 picker | `EditorView.swift:674-701` | `EditorRoute.kt`, `CalendarMediaPickerSheet.kt` | B01~B10 |
| 퀵 길이 | `EditorView.swift:703-730` | `EditorRoute.kt:1846-2211` | C11 |
| 컬렉션 player/import | `EditorView.swift:736-793` | `HomeRoute.kt`, `MovieCollectionSection.kt` | E09~E12, D09~D15 |
| 포스터 AI 후보 | `EditorView.swift:794-796` | `MovieCollectionSection.kt:914-1325` | E10 |
| 영상/음악 파일 | `EditorView.swift:797-835` | `EditorRoute.kt:1171-1247` | B01, C15~C18 |
| 브라우저 | `EditorView.swift:836-846` | `OnlineMusicBrowserRoute.kt` | E05~E08 |
| AiShot | `EditorView.swift:847-862` | `AiShotRoute.kt` | E01~E04 |
| 트림 | `EditorView.swift:863-961` | `VideoTrimSheet.kt` | C10 |
| 시사회/저장 | `EditorView.swift:962-975` | `PreviewRoute.kt` | D01~D15 |
| 카피라이터/정보 | `EditorView.swift:976-978` | `HomeRoute.kt:1367-2087` | A12~A14 |
| 자막 | `EditorView.swift:979-1017` | `TextOverlaySheet.kt` | C12~C14 |
| 음악 | `EditorView.swift:1018-1051` | `MusicSettingsSheet.kt` | C15~C18 |
| 엔딩 | `EditorView.swift:1052-1076` | `EndingInfoSettingsSheet.kt` | C19~C20 |
| deep link/shortcut | `HanClipApp.swift:615-665` | `HanClipQuickAction.kt`, `shortcuts.xml` | A02~A03 |
| share extension | `HanClipShare/ShareViewController.swift` | SEND/SEND_MULTIPLE | A05 |
| lock widget | `HanClipWidget/HanClipLockWidget.swift` | quick widget | A04 |
| purchase manager | `CopyrightPurchaseManager.swift` | 없음 | A14 |

역대조 결과, 위 iOS root presentation과 app-extension/URL/shortcut/purchase 진입점은 모두 최소 한 개 감사 ID에 연결했다. 다만 **실기기에서만 나타나는 OS picker 변형, 제조사 모션포토 변형, 폴드 hinge, 접근성 focus 순서, GPU/codec별 출력은 소스만으로 완전 확인할 수 없어 미검증**이며 동일 판정하지 않았다.
