# AiShot 카메라

## 역할/경로

- 홈 preset, media menu, quick action에서 full-screen. impact를 감지해 trigger 전후 ring buffer 영상을 저장하고 project에 clip 추가.
- dismiss 시 empty AiShot project 삭제; app interruption 후 조건을 만족하면 120ms 뒤 camera cover 재시작.
- 소스 `EditorView.swift:802-817,3315-3420`; `AiShotCamera.swift:1-2600`.

## UI 계층/배치

- black camera surface. GeometryReader에서 preview는 width×4/3을 넘지 않고 화면 가운데; previewTop/Bottom 계산.
- status badge(`감지 중/감지 됨/저장 중`), close, intro/golf guide, side settings.
- 좌우 side control 폭 116, 중심에서 ±126 기준; duration panel 116×52/radius12, timer tile30×30/radius9.
- bottom: sensitivity chips, zoom lens buttons/drag bar/precision dial, camera front/back, manual capture. saving 중 하단 progress bar.

## 표면/상태 색

- camera 위 panel은 black 78~92%와 Main/Sub gradient. white text/stroke. ready/detected green rgb(.31,.82,.54), detecting-not-ready amber rgb(.95,.70,.24), saving danger color는 소스 token.
- duration notice 280×116/radius16/padding14, 1.7s; current option Main/Sub gradient.

## 텍스트/아이콘

- 감도: `시끄러움` (`speaker.wave.3.fill`), `일반` (`speaker.wave.2.fill`), `조용함` (`speaker.wave.1.fill`), `자동` (`wand.and.stars`).
- 시간: `짧게` 앞1.5/뒤1.5=3s, `일반` 앞2/뒤3=5s, `길게` 앞5/뒤5=10s. icon `timer`.
- camera text `전면/후면`; zoom factor x와 focal length; phase exact text 위 참조.

## 동작

- 감도 tap=audio/visual detector threshold 변경.
- duration panel tap: normal→alternating long/short edge, short/long→normal. project별 저장.
- zoom lens tap/drag, precision dial; camera switch 후 zoom clamp. 저장 중에도 zoom/감도/camera 설정은 동작하고 progress만 별도 overlay가 되어야 한다.
- manual capture button은 detector와 별개 trigger. close는 capture/session 정리.

## 상태

- camera/mic permission unknown/denied/authorized; no device; session starting; detecting not ready/ready; detected buffering; saving progress; complete; cancel/error/interruption/restart.
- saving 실패는 파일 정리+alert. background/foreground는 session lifecycle에 맞춘다.

## 타이포/접근성

- duration main 14 bold; timer14 bold; notice label11 semibold/main14 bold/total13 heavy; options12 bold.
- duration accessibility value는 `<title>, 앞 n초, 뒤 n초`; hint 순환. zoom은 factor label/value/increment/decrement actions.

## 반응형

- portrait camera 4:3 center. side controls는 max/min x로 작은 폭에서 66pt 이상 확보. landscape/iPad 전용 레이아웃은 확인 필요.

## 컨트롤 기본값/수명

- [CONTROL_DEFAULTS.md](../CONTROL_DEFAULTS.md#AiShot). 감도는 앱 영속 자동; 시간은 project-scoped 일반; camera back/zoom1은 camera session; phase는 매 session detecting.

## Android

- CameraX + AudioRecord/visual analysis로 의미 유지. iOS ring buffer timing과 동일 전/후 seconds. 권한은 camera+microphone를 분리 안내. 저장 중 설정 controls를 막지 않는다.
