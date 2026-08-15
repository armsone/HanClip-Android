# HanClip 매치업 1차 감사

## 고정 기준

- iOS: `31e60ec5` + 2026-08-13 최종 작업 트리, `1.0.1 (3.11.44)`.
- Android 시작: `347ac14`, `1.0.1 (544)`.
- 시작 시 기존 변경은 `PROJECT_RULES.md`와 `HomeRoute.kt`의 만든 사람 GitHub 카드였다. 규칙 문서는 사용자 변경으로 남기고, 만든 사람 카드는 앞선 앱 요청 사항이므로 빌드 545 마무리 범위에 포함했다.
- 현재 iPhone PNG 4장은 최신 iOS 타이포·프리셋 높이 변경 전 자료이므로 구조 참고용이다. 현재 수치는 고정 기술서와 읽기 전용 Swift를 우선한다.

## 전체 호출 화면 감사

`동일`은 현재 양쪽 캡처와 시험이 모두 있어야 하므로 이번 1차에는 부여하지 않았다.

| ID | iOS 계약 | Android 근거 | 판정 | 다음 대표 상태 |
|---|---|---|---|---|
| HOME | 프리셋·영화·컬렉션·공유·테마 | `HomeRoute.kt` | 구현됨-미검증 | empty/populated/shared/busy |
| MEDIA_MENU | AiShot/사진/달력/파일 | `HomeRoute.kt`, `EditorRoute.kt` | 구현됨-미검증 | open |
| PHOTO | 필터·정렬·핀치·드래그 선택 | `CalendarMediaPickerSheet.kt` | Android 드래그 검증 | loading/empty/filter |
| CALENDAR | 월·날짜·오늘/전날 단계 선택 | `CalendarMediaPickerSheet.kt` | 구현됨-미검증 | month/today/selected |
| IMPORT | 진행·취소·rollback | `EditorRoute.kt`, `MediaImportReader.kt` | 구현됨-미검증 | progress/cancel/error |
| QUICK_DURATION | sticky CTA·공용 설정·비율 | `EditorRoute.kt` | Android 1.3배 검증 | settings-return |
| EDITOR | 설정·클립·저장/홈 | `EditorRoute.kt`, `EditorViewModel.kt` | 구현됨-미검증 | empty/populated/expanded |
| CLIP_TRIM | 사진/영상·이전/다음·삭제 | `VideoTrimSheet.kt` | 구현됨-미검증 | photo/video/confirm |
| TEXT | 사용·프리셋·서체·위치 | `TextOverlaySheet.kt` | 구현됨-미검증 | default/custom/font |
| MUSIC | 샘플·파일·브라우저·믹스 | `MusicSettingsSheet.kt` | 구현됨-미검증 | none/sample/file |
| BROWSER | 즐겨찾기·탐지·다운로드 | `OnlineMusicBrowserRoute.kt` | 구현됨-미검증 | default/favorites/download/error |
| ENDING | 5테마·시간·미리보기 | `EndingInfoSettingsSheet.kt` | 구현됨-미검증 | off/themes/duration |
| GENERATION | 준비·진행·취소·오류 | `EditorRoute.kt`, `ExportProgressPolicy.kt` | 구현됨-미검증 | progress/cancel/error |
| PREVIEW | 재생·다시 편집·공유·개봉 | `PreviewRoute.kt` | 구현됨-미검증 | paused/playing/fullscreen |
| RELEASE | 사진/파일 저장 | `PreviewRoute.kt`, `VideoSaveShare.kt` | 구현됨-미검증 | options/progress/error |
| COLLECTION | 0/1/29/30·핀·일괄 압축 | `MovieCollectionSection.kt` | 구현됨-미검증 | boundary counts/progress |
| COLLECTION_PLAYER | seek·zoom·pan·닫기 | `MovieCollectionSection.kt` | 구현됨-미검증 | portrait/landscape/zoom |
| COLLECTION_POSTER_AI | 후보 8+8·재생성 | `MovieCollectionSection.kt` | 구현됨-미검증 | loading/candidates/error |
| COLLECTION_COMPRESS | 1080/720/540·취소 | `MovieCollectionSection.kt`, `MovieCollectionStore.kt` | 구현됨-미검증 | options/progress/cancel |
| AISHOT | 권한·대기·촬영·저장 | `AiShotRoute.kt` | 구현됨-미검증 | permission/ready/capture/save |
| THEME | 순환·2초 알림·패널·재정렬 | `HomeRoute.kt` | 구현됨-미검증 | short-tap notice/panel |
| COPYRIGHT | 워터마크·절전·정보·라이선스 | `HomeRoute.kt` | 구현됨-미검증 | collapsed/expanded |
| PERMISSION_ALERT | 설명·설정·재시도 | 각 route 권한 패널 | 구현됨-미검증 | denied/permanent/recovered |

## 첫 비교에서 확인한 결과

- 최신 iOS Swift와 Android의 프리셋은 모두 기본 높이 `74 + 72 = 146`, 3/2/1열, 아이콘·제목·설명 3등분, 설명 10.4 크기를 사용한다. 오래된 iPhone PNG만 카드가 더 낮아 보이므로 PNG에 맞춰 Android를 되돌리지 않는다.
- Android 기본 글자 1.0에서는 6개 프리셋, 설명, 영화 목록과 컬렉션 제목이 잘리지 않았다.
- Android 글자 2.0에서는 1열로 바뀌고 카드 내용은 보존된다. 전체 페이지 스크롤이 필요하며 이는 의도한 접근성 차이다.
- 로고 짧은 탭은 테마만 바뀌고 iOS의 2초 확인 문구가 빠져 있었다. 이번 변경에서 `“<테마>로 변경했습니다.”` 캡슐을 복원했다.
- 퀵모드는 1.0배에서 비율과 만들기 버튼이 모두 보였지만 1.3배 첫 진입에서는 만들기 버튼이 내비게이션 영역 아래로 절반 잘렸다. CTA를 스크롤 내용과 분리한 고정 하단 버튼으로 옮기고 목록에는 CTA와 내비게이션 높이만큼 하단 여백을 확보했다. 최종 첫 진입 캡처에서 CTA 전체 노출을, 아래로 스크롤한 캡처에서 화면 비율 6개가 CTA 위에 모두 노출되는 것을 각각 확인했다.
- 사진 그리드는 한 번의 드래그로 건너뛴 셀을 포함해 8개가 선택되고, 같은 손가락을 반대 방향으로 되돌리자 앵커부터 현재 셀까지 3개만 남아 지나간 셀이 다시 해제되는 것을 에뮬레이터 중간 캡처로 확인했다. 두 드래그 중 모두 하단 필터·전날·오늘·해제·추가 행이 숨겨졌다. 선형 범위, 방향 반전 복원, 상·하단 가장자리 진행률과 probe 위치의 JVM 회귀시험도 통과했다.

## 재생성

```sh
./gradlew :app:assembleDebug
tools/matchup/capture_android_home.sh emulator-5554 app/build/outputs/apk/debug/app-debug.apk
tools/matchup/capture_android_quick.sh emulator-5554 sample-1.png 1.3 app/build/outputs/apk/debug/app-debug.apk
```

iOS는 현재 저장소 정책상 읽기 전용이며 설치된 시뮬레이터 앱도 `1.0.1 (3.9.18)`로 고정 기술서보다 오래됐다. 최신 iOS 카탈로그는 새 빌드 없이 재생성할 수 없으므로 이번 비교에서는 `reference/ios-current/home`의 고정 PNG와 최신 Swift/기술서를 함께 사용했다.
