# HanClip 강화 매치업 원자 비교 매트릭스

기준 시각: 2026-08-15. iOS 최신 작업 트리와 Android 현재 작업 트리를 비교한다. 이 문서의 `확인 필요`는 완료가 아니며, 같은 fixture/profile의 양쪽 post-change PNG와 기능 trace가 확보되기 전에는 `matched`로 바꾸지 않는다.

## 고정 프로필과 증거 품질

- `P0`: ko-KR, Asia/Seoul, automatic/system light, 기본 글자 크기, portrait, 최초 실행/empty. iOS 26.5 iPhone 17 Pro 1206×2622=402×874pt; Android API 37 `StarterApp_API_37`, `wm size 1206x2622`, `wm density 480`=402×874dp.
- `P1`: P0 + populated deterministic media/project/collection fixture.
- `P2`: dark + 접근성 글자 2.0, portrait.
- `P3`: light + 글자 1.3, portrait.
- `P4`: populated + landscape/player.
- 기존 `reference/ios-current/home/*.png`와 `reference/matchup/android/phone/*.png`는 원본 PNG와 SHA-256을 보존했지만 최신 iOS 빌드/동일 fixture가 아니므로 구조 참고만 가능하다. exact geometry/color/raster 판정에는 부적합하다.
- exact 수치는 iOS/Android 소스와 최신 런타임 capture를 함께 확인한다. OS-owned status/navigation bar 이외의 마스크는 허용하지 않는다.

| Route/state ID | Element/anatomy | Dimension/action | Fixture/profile | iOS exact reference | Android observed | Difference | Required action | Evidence/confidence | Status/exception proof |
|---|---|---|---|---|---|---|---|---|---|
| HOME/empty | page background | RGBA/gradient/app bounds | P0; cold launch | `ios/current/home_empty_default.png` | `android/phone/home_empty_default_r03.png` | automatic-light 최초 iOS vertical gap은 iOS runtime 결함 | gap 복제 금지; explicit Light와 상태 분리 | source+PNG High | PASS |
| HOME/empty | header logo | asset/bounds/gap/tap/long press | P0; cold launch; tap/0.6s hold | `HanClipTopHeader`, `LogoMarkV2` | `HanClipBrandCapsule` | 최신 bounds 미측정 | bounds+trace 비교 | source High | 확인 필요 |
| HOME/empty | header actions | i button + media button, order/bounds/icons | P0 | `EditorView` home header | `HomeHeader` | 최신 pair 없음 | anatomy별 capture 비교 | source High | 확인 필요 |
| HOME/empty | preset section | section icon/title/baseline/insets | P0 | `영화 프리셋` | `영화 프리셋` | runtime 미측정 | baseline/spacing 측정 | source High | 확인 필요 |
| HOME/empty | preset card | icon/title/subtitle/3-zone geometry | P0; six cards | 146pt, 10.4pt subtitle | Android 146dp, 10.4sp; custom film/golf vectors | OS font raster와 media-add path만 P2 | immutable r03 pair | source+PNG High | PASS |
| HOME/empty | movie section | `0/10`, title, skeleton rows | P0 | latest iOS capture pending | Android old capture exists | fixture/build 불일치 | fresh pair | screenshot Low | 확인 필요 |
| HOME/empty | collection section | `0/30`, add poster, floating i | P0 | latest iOS capture pending | Android old capture exists | fixture/build 불일치 | fresh pair | screenshot Low | 확인 필요 |
| HOME/populated | project row | artwork/title/badges/metadata/actions | P1; fixed project fixtures | `ProjectRow` | `SavedMovieRow` | deterministic pair 없음 | seed same metadata; anatomy rows | source Medium | 확인 필요 |
| HOME/populated | delete | reveal→delete→confirmation→side effect | P1; swipe 90pt then tap | iOS contract | Android `pendingDeleteSummary` | paired trace 없음 | record pre/input/post/relaunch | source High | 확인 필요 |
| HOME/shared | shared inbox | banner/progress/cancel/new/add | P1; fixed shared files | iOS source | Android `SharedInboxPanel` | pair 없음 | deterministic fixture+trace | source Medium | 확인 필요 |
| MEDIA_MENU/open | menu card | four rows/order/exact labels/icons | P0; media button tap | AiShot/사진/달력/파일 | trailing 250dp, row44, r34, same order | header media-add path는 P2 | r03 pair | source+PNG High | PASS |
| THEME/notice | notice capsule | exact theme text/duration/position | P0; logo tap | 2-second notice | Android notice restored | duration/runtime pair 없음 | timed trace + captures | source High | 확인 필요 |
| THEME/panel | panel | title/swatches/6 rows/reorder/confirm | P0; logo hold | iOS max620 | Android dialog | geometry/drag pair 없음 | capture + reorder persistence trace | source Medium | 확인 필요 |
| COPYRIGHT/collapsed | header | logo/X/reset/title | P0; i tap | latest iOS `ImportantInfoSheet` | `SettingsInfoScreen` | fresh pair 없음 | paired capture | source High | 확인 필요 |
| COPYRIGHT/collapsed | creator link | placement/text/icon/action | P0 | watermark 앞, min42/r14 | Android 동일 order/geometry | 없음 | cold tap opens system browser는 별도 외부 action | source+PNG High | PASS |
| COPYRIGHT/collapsed | watermark row | icon/title/use state/expand | P0; no stored key | default enabled when entitled | Android 테스트 무료 default enabled | StoreKit/Play product state 차이 | 승인된 product exception | source+PNG+trace High | PASS/exception |
| COPYRIGHT/expanded | platform grid | 10 assets/5 columns/selection | P0; expand | iOS 10 platforms | Android 10 platforms | bounds/tints unverified | paired capture | source High | 확인 필요 |
| COPYRIGHT/expanded | address field | per-platform text persistence | P0; select Instagram→type→switch→return→cold launch | iOS per-platform UserDefaults | Android per-platform SharedPreferences | 없음 | `hanclip_test` AX tree 재확인 | source+runtime High | PASS |
| COPYRIGHT/expanded | custom icon | picker/preview/persistence/fallback | P1; select Custom | iOS PhotosPicker + square preview | Android SAF + square preview/store | system picker만 OS-owned | deterministic media fixture 추가 필요 | source High | 구현; runtime 확인 필요 |
| COPYRIGHT/expanded | position grid | 5×5 geometry/selection/a11y | P0 | iOS 14/7 circles in 28 visible rows | Android 14/7 circles in 28 visible area, 48dp target | Play 무료 editor state | 승인된 product exception | source+PNG High | PASS/exception |
| COPYRIGHT/expanded | colors | text/shadow controls + arbitrary color | P0 | two ColorPickers | two validated HEX color editors | native picker UI differs | app-owned post-selection state 비교 필요 | source High | 구현; runtime 확인 필요 |
| COPYRIGHT/expanded | shadow opacity | 0…100 step10 + persistence | P0; tap repeatedly/relaunch | iOS button | Android cyclic 10% button/store | 없음 | related persistence store test | source High | PASS |
| COPYRIGHT/sleep | sleep control | 3 options/text/persistence | P0; change/relaunch | AiShot always true; exact Automatic text | Android same geometry/text/policy | 없음 | `SleepPreventionPolicyTest` | source+PNG+test High | PASS |
| PHOTO/entry | app bar | cancel/title/count action bounds | P1; open Photo | iOS 88×40 zones | Android picker header | geometry pair 없음 | capture P1 | source High | 확인 필요 |
| PHOTO/entry | grid cell | thumbnail/badge/check/scale/order | P1; 8 deterministic assets | iOS 5 columns default | Android source | same fixture unavailable | seed/capture | source Medium | 확인 필요 |
| PHOTO/entry | bottom controls | filter/yesterday/today/clear/add | P1 | iOS 54×54 | Android controls | exact anatomy unknown | capture+measure | source Medium | 확인 필요 |
| PHOTO/drag | range selection | forward/reverse/edge autoscroll | P1; fixed drag coordinates | iOS trace pending | Android old trace exists | no paired trace | execute same gesture | Android runtime High | 확인 필요 |
| PHOTO/permission | denied surface | explanation/settings/retry | P0; denied | iOS known product deficiency | Android explanatory recovery | product behaviors differ | record as explicit product override, not OS exception | source High | remaining |
| CALENDAR/month | grid | 4–6 weeks/day/caption/disabled | P1; fixed Aug 2026 | iOS source | Android source | capture/fixture 없음 | seed same dates | source Medium | 확인 필요 |
| CALENDAR/selected | selection | ordered IDs/Photo round trip | P1; select 3 dates | shared iOS array | shared Android session | paired trace 없음 | record order after round trip | source High | 확인 필요 |
| IMPORT/progress | progress surface | message/count/bar/cancel | P1; import 8 files | iOS transaction | Android transaction | visual/trace pair 없음 | deterministic slow fixture | source High | 확인 필요 |
| IMPORT/error | rollback | failure message/no partial clips | P1; invalid item | iOS rollback | Android rollback tests | paired state 없음 | injected failure trace | test Medium | 확인 필요 |
| QUICK_DURATION/default | header | close/title/media action | P1; Quick + 1 photo | iOS source | Android old capture | current pair 없음 | fresh pair | source High | 확인 필요 |
| QUICK_DURATION/default | choices | 8 labels/2 columns/selection | P1 | exact labels in spec | Android exact labels | geometry pending | fresh pair | source High | 확인 필요 |
| QUICK_DURATION/default | sticky CTA | text/height/nav inset/action | P1 | 93pt region | Android fixed CTA | current pair 없음 | capture top/bottom + tap | source High | 확인 필요 |
| QUICK_DURATION/roundtrip | settings state | caption/music/ending/media return | P1 | iOS state preserved | Android state path | paired trace 없음 | same trace/relaunch | source Medium | 확인 필요 |
| EDITOR/empty | settings panel | collapsed title/badge/chevron | P0; New Movie | iOS source | Android source | fresh pair 없음 | capture | source High | 확인 필요 |
| EDITOR/populated | setting row | each icon/label/detail/control geometry | P1; mixed fixture | iOS 8 rows | Android 8 rows | atomic captures missing | expanded pair + row matrix | source High | 확인 필요 |
| EDITOR/populated | clip row | position/thumb/time/mode/stepper | P1 | iOS `ClipRow` | Android `ClipCard` | pair 없음 | capture same clips | source Medium | 확인 필요 |
| EDITOR/reorder | group drag | tile/order/persistence | P1; mixed groups | iOS group semantics | Android reorder | paired trace 없음 | drag+save+relaunch | source Medium | 확인 필요 |
| EDITOR/bottom | make bar | close/ratio/summary/CTA | P1 | iOS 52pt controls | Android sticky bar | bounds pending | capture+tap/drag trace | source High | 확인 필요 |
| CLIP_TRIM/photo | header/player | close/delete/prev-next/photo preview | P1 | `VideoTrimEditor` | `VideoTrimSheet` | pair 없음 | capture+trace | source Medium | 확인 필요 |
| CLIP_TRIM/video | waveform | trim bars/peaks/time/play/loop | P1; fixed video | iOS source | Android source | rendered pair 없음 | deterministic media capture | source Medium | 확인 필요 |
| CLIP_TRIM/delete | confirmation | exact title/body/result | P1; tap delete | iOS exact contract | Android confirmation | trace pending | record exact text/state | source High | 확인 필요 |
| TEXT/default | header | reset/cancel/save semantics | P1; open caption | iOS snapshot rules | Android snapshot tests | visual/trace pair 없음 | capture + cancel/save trace | tests Medium | 확인 필요 |
| TEXT/default | preview | placeholder/text/font/color/shadow | P1 | iOS preview | Android preview | raster pair 없음 | same strings/settings capture | source Medium | 확인 필요 |
| TEXT/custom | controls | preset/font/import/position/color | P1 | iOS source | Android source | pair 없음 | capture + persistence trace | source Medium | 확인 필요 |
| MUSIC/none | rows | none/sample/file/browser/mix | P1 | iOS source | Android source | pair 없음 | capture | source Medium | 확인 필요 |
| MUSIC/sample | sample card | artwork/title/detail/play/selected | P1; fixed sample | iOS six cards | Android six cards | pair 없음 | same sample capture/trace | source High | 확인 필요 |
| MUSIC/file | import | picker/validation/name/relaunch | P1; deterministic audio | iOS document picker | Android SAF | picker OS-owned only; app result pending | trace post-picker | source Medium | 확인 필요 |
| BROWSER/default | chrome | close/address/favorite/nav/webview | P0; fixed URL | iOS browser | Android browser | pair/network unstable | local deterministic page | source Medium | 확인 필요 |
| BROWSER/favorites | row | favicon/title/WWW badge/trailing action | P1; fixed favorites | iOS anatomy | Android anatomy | pair 없음 | capture each anatomy | source Medium | 확인 필요 |
| BROWSER/download | panel | 영상/받기/닫기/progress/cancel | P1; local page | iOS source | Android source | pair 없음 | local server trace | source Medium | 확인 필요 |
| ENDING/off | header/control | enable/duration/5 themes | P1 | iOS source | Android source | pair 없음 | fresh pair | source High | 확인 필요 |
| ENDING/theme | preview | renderer geometry/colors/text | P1; fixed stops/dates | iOS renderer | Android Canvas renderer | pixel comparison 없음 | export/capture same ratio | source Medium | 확인 필요 |
| ENDING/variation | reselection | TreasureMap variation increments | P1; tap selected theme | iOS increments | Android path | paired trace 없음 | execute+persist trace | source Medium | 확인 필요 |
| GENERATION/progress | overlay | blur/panel/thumb reveal/messages/time/cancel | P1; deterministic export | iOS source | Android overlay/service | pair 없음 | slow injected export | source Medium | 확인 필요 |
| GENERATION/error | alert | exact source error/no stuck busy | P1; invalid media | iOS alert | Android alert | trace pending | injected failure | tests Medium | 확인 필요 |
| PREVIEW/playing | player frame | header/square player/fullscreen/progress | P1; fixed output | iOS source | Android source | pair 없음 | capture frame0/playing | source Medium | 확인 필요 |
| PREVIEW/actions | bottom bar | 다시 편집/share/개봉하기 | P1 | exact labels/icons | Android exact labels | bounds pending | capture+destinations | source High | 확인 필요 |
| PREVIEW/fullscreen | gestures | autoplay/seek/scrub/zoom/pan/close | P4 | common iOS player | Android player | paired trace 없음 | same gesture trace | source Medium | 확인 필요 |
| RELEASE/options | card | title/photo+album/file/cancel | P1 | iOS source | Android source | pair 없음 | paired capture | source High | 확인 필요 |
| RELEASE/result | save | progress/success/error/file name | P1 | iOS Photos/Files | Android MediaStore/SAF | system picker differs; app result pending | mask only picker, trace result | source Medium | 확인 필요 |
| COLLECTION/empty | shelf | count/add/bulk disabled | P0 | iOS source | Android source | fresh pair 없음 | capture | source High | 확인 필요 |
| COLLECTION/populated | poster | image/title/pin/metadata/menu | P1; 1/29/30 fixtures | iOS source | Android fixtures exist | pair 없음 | seed same metadata/capture | source Medium | 확인 필요 |
| COLLECTION/reorder | pin drag | hit layer/order/relaunch | P1; 8 pinned | iOS source | Android previously device-tested | no paired trace | same drag trace | Android runtime Medium | 확인 필요 |
| COLLECTION_POSTER_AI/candidates | grid | 8+8 labels/cards/overlay | P1; fixed video | iOS source | Android source | deterministic candidates differ | compare layout/semantics; content state classify | source Medium | 확인 필요 |
| COLLECTION_COMPRESS/options | sheet | current info/1080/720/540/estimate | P1 | iOS source | Android source | pair 없음 | capture | source High | 확인 필요 |
| COLLECTION_COMPRESS/progress | transaction | progress/cancel/original retention | P1 | iOS contract | Android transaction tests | paired trace 없음 | deterministic short video trace | tests Medium | 확인 필요 |
| COLLECTION_PLAYER/portrait | player | title/close/share/video/controls | P1 | common iOS player | Android player | pair 없음 | capture | source Medium | 확인 필요 |
| COLLECTION_PLAYER/landscape | player | safe areas/fit-fill/gestures | P4 | iOS current-window policy | Android orientation policy | device pair 없음 | landscape captures+trace | source Medium | 확인 필요 |
| AISHOT/permission | prompt/recovery | camera+mic denied/retry/settings | P0 | iOS status | Android status | platform prompt OS-owned, app recovery pending | mask prompt only; capture app surfaces | source Medium | 확인 필요 |
| AISHOT/ready | camera UI | preview/status/close/duration/sensitivity/zoom/camera/capture | P0; no recording | iOS source | Android CameraX | paired device capture 없음 | physical-device trace needed | source Medium | 확인 필요 |
| AISHOT/capture | phase | detecting→detected→saving→ready | P1; controlled audio/manual | iOS ring buffer | Android implementation | paired trace 없음 | manual trigger first; record timings | source Medium | 확인 필요 |
| PERMISSION_ALERT/denied | alert | exact reason/recovery/back | P0; each permission denied | iOS app-owned alert | Android app-owned alert | full matrix not captured | per-permission traces | source Medium | 확인 필요 |
| RESPONSIVE/font2 | home/editor/settings | columns/wrap/scroll/no clipping | P2 | iOS Dynamic Type | Android fontScale2 | iOS pair 없음 | captures + bounds overflow scan | source Medium | 확인 필요 |
| RESPONSIVE/landscape | editor/player | orientation/state preservation | P4 | iPhone player; iPad all routes | Android player/root policies | broad pair 없음 | representative path evidence | source Medium | 확인 필요 |
| ACCESSIBILITY | repeated components | label/role/state/action/touch target | P0–P4; screen reader trees | iOS accessibility tree | Android semantics tree | paired trees absent | export trees and compare atomically | source Medium | 확인 필요 |

## 현재 분류 합계

- Visual: 모든 route의 fresh paired capture가 필요하다.
- Content/state: deterministic populated fixture가 아직 양쪽에 공통으로 없다.
- Functional: 카피라이터의 주소·사용자 아이콘·색상·그림자 투명도는 Android 카피라이터 화면에서 누락된 것이 소스로 확정됐다.
- Forced OS exception: 아직 승인된 항목 없음. 상태 표시줄·내비게이션 바·시스템 picker/permission prompt도 실제 최소 영역과 public API 제약 증거가 생긴 뒤에만 기록한다.
