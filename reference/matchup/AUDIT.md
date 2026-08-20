# HanClip Android 강화 매치업 감사

## 2026-08-21 Ai 0.5.0 증분

- iOS 기준은 `4c251444` 위 미커밋 8개 파일이며 SHA-256과 제품 계약을 `reference/ios-current/design-spec/AI_HIGHLIGHT_0_5_0.md`에 고정했다.
- Android는 무트랙·무음 영상의 프레임 움직임 분석, 중앙 폴백, 분석 출처 저장·복원, 트림 배지·접근성 설명을 구현했다.
- AiShot은 100ms 국소 움직임 상태기계와 충격음 결합, bundled ML Kit 관절 보조, 저전력·발열 간격 조절 및 관절 실패 시 움직임 경로 복귀를 구현했다.
- 순수 상태기계와 결합 정책은 JVM 집중 시험으로 확인했다. Android Kotlin 컴파일은 통과했다.
- 새 상태의 안정적인 iOS lossless capture와 공통 무음/골프 fixture가 없어 시각·행동 parity 완료 판정은 보류한다. 소스 기준 구현 완료이며 `CLIP_TRIM/video/no-audio`, `AISHOT/motion-fusion`, `AISHOT/pose-fusion`은 `MATRIX.md`에서 paired runtime 확인 필요로 유지한다.

기준 시각은 2026-08-15이며, iOS `1.0.1 (3.11.47)` 현재 작업 트리를 source of truth로 사용했다. Android 후보는 base `c230ddb2` 위의 현재 변경분이다.

## 판정

- r03 immutable 원본과 SHA-256을 양쪽 작업에서 독립 재계산했다.
- 이번 stable pair 범위의 P0/P1은 없다.
- 합격 범위: `HOME.empty.automatic-light`, `HOME.empty.dark`, `MEDIA_MENU.open.automatic-light`, `COPYRIGHT.collapsed.dark`, `COPYRIGHT.expanded.dark`.
- 기능 합격: 기본 카피라이터 로고 사용, 플랫폼별 주소 저장/왕복/강제 종료 후 cold relaunch 복원, AiShot의 화면 유지 우선 정책.
- 전체 앱 parity 완료 판정은 아니다. iOS가 stable paired-ready 원본을 제공하지 못한 나머지 route/state는 `MATRIX.md`에서 `확인 필요`로 유지한다.

## 고정 profile

- iOS: iPhone 17 Pro, iOS 26.5, 1206×2622, 402×874pt, scale 3, ko-KR, Asia/Seoul, 기본 Dynamic Type, fresh-empty-v1.
- Android: `StarterApp_API_37`, API 37, `wm size 1206x2622`, `wm density 480`, 402×874dp, portrait, ko-KR, Asia/Seoul, font scale 1.0, fresh-empty-v1.
- 최초 HOME는 양쪽 모두 theme mode `automatic` + system light다. explicit Light Mode와 합치지 않는다.

## r03 원본

| State | Android file | SHA-256 | 판정 |
|---|---|---|---|
| HOME automatic-light | `android/phone/home_empty_default_r03.png` | `5b7f58eaf9a9f5f06e918212c48523dd7da19aa5e1110f2b75658cd288ec3ff8` | PASS |
| MEDIA_MENU open | `android/phone/media_menu_open_r03.png` | `e6261a9e8858f88e0cca1bfc3c2c74cba5d922c02f566c798dd566aa81958d8d` | PASS |
| HOME dark | `android/phone/home_empty_dark_r03.png` | `2bb9e557ea0056a564cf659bbeeb81f4d348016d49ef5f73da6d1a9b7dccec6d` | PASS |
| COPYRIGHT collapsed dark | `android/phone/copyright_collapsed_dark_r03.png` | `a61dde0d587a2c0e82383e009f8ec498406d417d1db9032df1c26d3b823840e3` | PASS + product exception |
| COPYRIGHT expanded dark | `android/phone/copyright_expanded_dark_r03.png` | `69b3528b23c2e65daa220a2f89e44acd7d96bb5c8ac4c54820d1753e3c16de46` | PASS + product exception |

Capture APK 545 SHA-256: `42685346a0b3cbbea9e410a4ccb4b1e2e7a121f349c591a879d0ee76cd18b39b`. 최종 설치 후보 releaseQa 546 SHA-256: `83cdf966782f4b965a72815611c190f2acb4b7b2627e2493fcaaf3fe0e487816`.

## 반영한 차이

- HOME의 146dp 카드, 3열, 14dp outer inset, 8/10dp gap, 40dp icon surface, 15sp/10.4sp text geometry를 유지했다.
- dark 카드에서 Compose shadow layer가 만든 직각 inset rectangle를 제거하고 light의 낮은 alpha shadow만 유지했다.
- 새 영화, 골프, preset section, 영화 목록, 컬렉션 glyph를 iOS 형태에 맞는 filled/custom vector로 교체했다.
- media menu를 trailing 250dp, 44dp row, radius 34dp로 맞추고 regular weight를 적용했다.
- COPYRIGHT title/creator/sleep/card/5×5 position geometry와 AX action을 맞췄다.
- 기본 로고는 사용 상태이며 전역 카피라이터 설정과 플랫폼별 주소를 프로젝트 caption 설정과 분리해 저장한다.
- AiShot은 `AlwaysOff`에서도 화면을 유지하며 Automatic 문구는 `렌더링, 사진/파일 가져오기, 저장 중에만 유지합니다.`다.

## 예외와 미검증

- 승인된 product exception: Android 테스트 기간 무료 카피라이터 editor를 iOS StoreKit 구매 카드로 바꾸지 않는다.
- 허용 P2: header media-add glyph의 플랫폼 path와 OS 글꼴 raster 차이.
- OS-owned status/navigation bar만 시각 비교에서 제외한다.
- BROWSER 웹 본문은 외부 동적 콘텐츠이며 app chrome만 증거로 사용할 수 있다.
- `THEME.notice.r03`은 Android 원본만 있고 안정적인 iOS pair가 없어 미검증이다.
