# iOS Ai 0.5.0 고정 기준 — 2026-08-21

기준 커밋은 iOS `4c251444`이며 아래 미커밋 작업 트리 파일의 SHA-256을 기준 시점에 고정했다. iOS 저장소는 읽기 전용으로 유지한다.

| 파일 | SHA-256 |
|---|---|
| `Models/ClipItem.swift` | `eeb9456eef61f98d537ab4c4b04a1b7a352e72e437644b2ac4f98bca5c58662b` |
| `Services/AiShotCamera.swift` | `f7c498c16fda3966546aebe1f297e307255d301640f150a683184e2f51662bd7` |
| `Services/AudioAnalysisService.swift` | `bd561615b9db18d336778c1e73549a38381b3d68908a0d797fa7a69da3fb8905` |
| `Services/AudioImpactClassifier.swift` | `c7dd5b39e3322d8580bb59626ca19cece012aadc86881ce1250509bb4801fbd9` |
| `Services/ProjectStore.swift` | `706ba993aa9b290959a8ffbcfb349775aff96a096b0822e4873d599e533da676` |
| `ViewModels/EditorViewModel.swift` | `033e41da7a2b929313b791ae98e3208b4e88eab239f6aa20871b969133bc5f20` |
| `Views/EditorView.swift` | `1b8156f0351ef4bff2c9a3b45b0d2d9a0b5548f9c19af1fe79ed89df11115782` |
| `Views/VideoTrimEditor.swift` | `00a3737365a8f20c22151bf4fc09278337bc2c396969ecd766614a902746ceaf` |

## 제품 계약

- 오디오 트랙이 없으면 `noTrack`, 트랙은 있으나 최대 peak `< 0.0032`이고 최대 RMS `< 0.0016`이면 `silent`로 기록한다.
- 무음 영상은 최대 1,200개의 160×90 프레임을 최소 0.2초 간격으로 분석한다. 장면 전환·플래시·국소 움직임 `< 0.0035`는 후보에서 제외한다.
- 화면 움직임 최대 점수가 `0.12` 미만이면 중앙을 선택하고 `화면 변화 적음 · 중앙 선택`, 그렇지 않으면 `화면 움직임 분석`을 파형 왼쪽 위에 표시한다.
- `audioAvailability`와 `highlightSource`는 프로젝트 저장·복원과 파생 자클립에 유지한다.
- AiShot 0.5.0은 100ms 화면 격자 분석, 200ms 관절 보조를 기본으로 사용한다. 저전력은 330ms, 심한 발열은 400ms, 임계 발열은 관절 분석을 중단한다.
- 화면 전체 변화·플래시·카메라 흔들림은 골프 스윙 근거에서 제외한다. 정지 자세→백스윙→다운스윙과 같은 시점의 충격음이 결합돼야 자동 촬영한다.
- 관절 보조가 최근 유효하면 어깨·골반·손목 흐름까지 확인한다. 사람을 잃거나 관절 분석을 쓸 수 없으면 화면 움직임+소리 경로로 복귀한다.

## 증거 한계

이번 기준에는 새 `CLIP_TRIM/video/no-audio`, `AISHOT/motion-fusion`, `AISHOT/pose-fusion`의 iOS lossless 런타임 PNG와 행동 trace가 없다. Android 구현은 소스 기준으로 검증하되 양쪽 안정 캡처 전에는 시각 parity 완료로 판정하지 않는다.
