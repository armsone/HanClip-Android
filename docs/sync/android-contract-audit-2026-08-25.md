# Android 계약 감사 2026-08-25

기준: `reference/ios-current/product-contract.yaml` (43개 계약, iOS dirty AiShot 0.6.0 포함).
상태는 이번 세션의 소스 수정·테스트 추가 이후 값이다.

- `same` = 소스 수준 동등(수치·상태·데이터 계약 일치). 이번 세션 구현분은 비고에 `2026-08-25 구현` 표기.
- `runtime_evidence_needed` = 소스는 존재하나 수용 판정에 실기기·페어드 fixture 증거가 필요.
- `intentional_platform_difference` = 기록된 의도적 차이(계약의 적응 허용 또는 사용자 결정).
- `implementation_needed` = 0건 (이번 세션에서 소스 수준 누락 전부 해소).

| # | 계약 ID | 상태 | Android 근거 | 남은 런타임 증거 |
| ---: | --- | --- | --- | --- |
| 1 | HC-ROOT-001 | runtime_evidence_needed | `MainActivity.kt`, `EditorRoute.kt` (루트 분기·busy overlay·단일 전역 alert) | SCREEN_MAP §B 30항목 전수 왕복(AT-002/003) |
| 2 | HC-HOME-001 | same | `DraftProjectStore.kt:211-335` (10/2/5 한도, 오래된 것부터·핀 보존 정리), 컬렉션 30 | AiShot 3개→1개 자동 삭제 실기기 확인 |
| 3 | HC-THEME-001 | same | `HanClipThemeMode.kt`, 테마 패널 재정렬(빌드 486/488), golf 색 전역 미포함 | REF-DESIGN §2 hex 페어드 비교 |
| 4 | HC-MEDIA-001 | runtime_evidence_needed | 1/3/5/8열 핀치, 드래그 선택, edge autoscroll (`MediaSortPolicyTest`, `MediaDragSelectionPolicyTest`) | '추가순' MediaStore 대응 실측(UNK-003) |
| 5 | HC-MEDIA-002 | same | 선선택 복원·보관함 출처만 교체·소실 asset 비파괴(빌드 451, `MediaPickerSavedStatePolicyTest`) | — |
| 6 | HC-MEDIA-003 | same | `CalendarMediaPickerSheet.kt`, 한국 공휴일 표시(빌드 517/518), `TodaySelectionPolicyTest` | — |
| 7 | HC-IMPORT-001 | same | `ImportFileTransaction.kt`+Test, `StorageSpaceGuardTest`, rollback 경계 계측(2026-08-12) | AT-004 전체 수동 시나리오 |
| 8 | HC-QUICK-001 | same | `QuickDurationPolicy.kt`+Test, 패널 복귀 흐름 | 자막 취소·음악 실패 왕복 화면 확인 |
| 9 | HC-PRESET-001 | same | 프리셋별 기본시간·자막·음악(빌드 522), 여행 엔딩 자동 활성화 금지(UNK-001) | 6프리셋 값 스냅샷 비교 |
| 10 | HC-EDITOR-001 | same | `DefaultDurationStepPolicyTest`, `VideoSegmentPolicyTest`, `EditorSessionPolicyTest`, isVideoSegmentSelected 비파괴 제외 | REF-AT §4 편집 체크리스트 |
| 11 | HC-EDITOR-002 | same | interval 기본 6(`EditorViewModel.kt:82`), 빈 묶음 fallback 대표(`EditorViewModel.kt:2127`) | 전체 해제 후 재로드 대표 1개 확인 |
| 12 | HC-TRIM-001 | same | `VideoTrimSheet.kt`, 192 bucket·peak 마커, `ProjectClipTimingPolicyTest` | 동일 입력 ±1 bucket 페어드 비교 |
| 13 | HC-ANALYZE-001 | same | `AudioAnalysisService.kt` impactScore 0.55/0.45/0.35, EMA 0.985/0.015, 결합 가중치 | 고정 PCM fixture iOS 수치 비교 |
| 14 | HC-ANALYZE-002 | same | 무음 0.0032/0.0016, baseline 0.94/0.06, 0.12 미만 중앙 fallback, 출처 문구 3종 | 무음 fixture 3종 분기 비교 |
| 15 | HC-TEXT-001 | same | `WatermarkSettings`+`IOSParityDefaultsTest`, `ImportedFontPolicyTest`, 폰트 소실 fallback | — |
| 16 | HC-MUSIC-001 | same | 기본 0.35/1.0/loop/fade 0.3·1.0, `daily-loop`·`golf-lets-go` stable ID | 기본값 스냅샷 자동화(후속 테스트) |
| 17 | HC-BROWSER-001 | same | `.hanclipfavorites`+`application/vnd.hanclip.browser-favorites+json` (manifest·`OnlineMusicBrowserRoute.kt:1337`) | iOS 내보내기 파일 실 상호 판독 |
| 18 | HC-ENDING-001 | same | 5테마·정보 부재 fallback·위치 무관 생성(빌드 460~481, `EndingInfoCardRenderer.kt`) | 정보 없음 fixture 렌더 확인 |
| 19 | HC-EXPORT-001 | same | export token 취소·temp 정리·foreground service (`ExportProgressPolicyTest`, `ExportFileTransactionTest`, `ExportForegroundPolicyTest`) | AT-005/AT-006 |
| 20 | HC-PREVIEW-001 | same | `VideoSaveShare.kt` 앨범명(빌드 487)·실패 시 결과 보존·SAF | AT-003 저장 실패 재시도 실기기 |
| 21 | HC-PLAYER-001 | runtime_evidence_needed | 공통 플레이어(scrub/zoom/swipe, 센서 방향), `FullscreenPlaybackPolicyTest` | AT-009 회전 중 위치·zoom 유지 |
| 22 | HC-COLL-001 | same | `MovieCollectionStore.kt` 30 한도·핀·메타 보존, `CollectionShelfPolicyTest` | fixture 5 저장 왕복 |
| 23 | HC-COLL-002 | same | 8+8 후보·posterSelectionVersion(빌드 497) | 오류 주입 포스터 보존 확인 |
| 24 | HC-COLL-003 | same | 더 작을 때만 안전 교체·재생 검증·temp 정리(빌드 496/518, 2026-08-12 감사) | AT-007 + 강제 종료 fixture 6 |
| 25 | HC-AISHOT-001 | intentional_platform_difference | Android는 클립이 생기기 전 프로젝트를 만들지 않고 닫기 시 캡처를 편집기로 전달 — 빈 AiShot 프로젝트가 남지 않는 동등 결과 | AT-008 + 촬영 0회 닫기 홈 무변화 |
| 26 | HC-AISHOT-002 | runtime_evidence_needed | 카메라+마이크 동시 권한, FHD 캡처→1080×1440 3:4 크롭, 방향 리스너 | 4방향 회전 저장 파일 재생 방향 |
| 27 | HC-AISHOT-003 | same | 3 프리셋(1.5/1.5·2/3·5/5)·edge 순환 영속·1.7초 notice·0.35초 재시작; **2026-08-25 구현**: 최소 0.5초 trim 검증 | 프리셋별 결과 길이 ±0.2초 실측 |
| 28 | HC-AISHOT-004 | same | 감도 14상수 표 일치·말소리 배제·자동 감도; **2026-08-25 구현**: 미터 clamp(score×4.5, 0.04..1) 수정, `RealtimeImpactClassifierTest` 신설 | PCM fixture 4종 iOS 페어드 비교 |
| 29 | HC-AISHOT-005 | same | 전이 임계 12종 일치; **2026-08-25 구현**: 전역 변화 시각 기록(퍼팅 장면 안정성 게이트) | 합성 격자 시퀀스 페어드 비교 |
| 30 | HC-AISHOT-006 | same | **2026-08-25 구현**: 자세 임팩트 창 시작 −0.15→−0.45 수정+경계 테스트; ML Kit 매핑은 적응 허용 | 2인·가림 시나리오 회귀 확인 |
| 31 | HC-AISHOT-007 | same | **2026-08-25 구현**: 융합 정책 재작성(모션 OR 자세 정렬, −0.20~+0.32 정책 내부화, 관측 conf≥0.72 자세 확인 게이트, 억제 창 강한 임팩트만) + 테스트 3종 | 타이밍 로그 재생 비교 |
| 32 | HC-AISHOT-008 | same | **2026-08-25 구현**: `GolfPuttStrokeAnalyzer`·`GolfPuttFusionPolicy`·`AiShotModelVersion(0.6.0)` 신설, 오디오 모니터에 무음 트리거 경로 배선, 트리거 시각 min(현재, strokeTime) 오프셋; `AiShotSoundlessPuttTest` 9케이스 | 실제 퍼팅 정확도(UNK-004 — 설계상 미검증 유지) |
| 33 | HC-AISHOT-009 | same | log octave 드래그·clamp·저장 중 전환 지연(pendingLensFacing)·세션 수명 줌, `AiShotZoomPolicyTest` | 저장 진행 중 전환 클립 무손상 |
| 34 | HC-AISHOT-010 | runtime_evidence_needed | ON_PAUSE 결과 폐기·ON_RESUME 재시작·인트로 스윙·saveWeight ring; 120ms 재시작은 lifecycle 관용 매핑(적응 허용) | AT-008/AT-009 백그라운드 왕복 자동 재개 |
| 35 | HC-SHARE-001 | same | ACTION_SEND/SEND_MULTIPLE→`SharedInboxStore.kt`, 배너·HC-IMPORT-001 공용 가져오기 | 이미지/영상/다중 3케이스 왕복 |
| 36 | HC-LINK-001 | same | 7개 URL 라우팅(`HanClipQuickAction.kt`)+3종 shortcut(실기기 확인, 빌드 507); 노출 확대는 UNK-005 대기 | URL cold start 재확인 |
| 37 | HC-COPY-001 | intentional_platform_difference | **2026-08-25 구현**: 기능 사전 Ai 이력 0.5.1·0.6.0 추가(`HomeRoute.kt:2429-2447`); Play Billing은 테스트 기간 전체 개방 결정으로 의도적 보류 | 결제 도입 시 샌드박스 검증 |
| 38 | HC-PERSIST-001 | same | temp→fsync→decode 검증→원자 교체→backup(`ProjectFileTransaction`+Test), 손상 격리, backup 복구 계측(2026-08-12) | AT-001 업그레이드·프로세스 kill 주입 |
| 39 | HC-PERM-001 | same | READ_MEDIA 분리·CAMERA/RECORD_AUDIO·설정 이동, 코드 검토상 프레임·좌표 업로드 없음 | AT-010 거부→설정→복귀 4종 왕복 |
| 40 | HC-DEVICE-001 | intentional_platform_difference | 920dp 상한 유지; 600/840/1200dp breakpoint는 Android 추가(UNK-002 기록) | 3기종 스크린 검사 |
| 41 | HC-A11Y-001 | runtime_evidence_needed | AiShot 컨트롤 semantics·롱터치 라벨 병합(빌드 507/510)·장식 요소 숨김 | TalkBack 전체 순회 스크립트 |
| 42 | HC-REL-001 | same | versionName 2.0.0(PROJECT_RULES §4), Ai 버전 독립 표기(`AiShotModelVersion.current`=0.6.0, 롤백=값 교체) | 정보 화면 표기 비교 |
| 43 | HC-TEST-001 | runtime_evidence_needed | JVM 테스트 39클래스(트랜잭션·정책·AiShot 판정, 이번 세션 +3) | REF-DATA §10 fixture 7종 완전 자동화, REF-AT P0 10건 수동 게이트 |

## 상태 합계

- same: 33 (이번 세션 구현 포함: HC-AISHOT-003/004/005/006/007/008)
- intentional_platform_difference: 3 (HC-AISHOT-001, HC-COPY-001, HC-DEVICE-001)
- runtime_evidence_needed: 7 (HC-ROOT-001, HC-MEDIA-001, HC-PLAYER-001, HC-AISHOT-002, HC-AISHOT-010, HC-A11Y-001, HC-TEST-001)
- implementation_needed: 0

## 이번 세션 변경 요약 (2026-08-25)

1. `AiShotMotionFusion.kt` — `AiShotModelVersion`(0.4.0~0.6.0, 롤백 플래그) 신설, 자세 임팩트 창 −0.45초 수정, `GolfSwingFusionPolicy` 계약 정합 재작성, 전역 변화 시각 추적, `GolfPuttStrokeAnalyzer`·`GolfPuttFusionPolicy`(HC-AISHOT-008) 신규.
2. `AiShotRoute.kt` — 모델 정보 0.6.0, 사운드 미터 clamp(score×4.5, 0.04..1), 관측 conf≥0.72 자세 확인 게이트, 무음 퍼팅 폴링 경로·트리거 시각 오프셋, 분류기 테스트 가시성.
3. `AiShotVideoTrimmer.kt` — 최소 0.5초 결과 구간 검증.
4. `HomeRoute.kt` — 기능 사전 Ai 이력 0.5.1/0.6.0(정직한 한계 포함).
5. 테스트 — `AiShotSoundlessPuttTest`(9), `RealtimeImpactClassifierTest`(4), `AiShotMotionFusionTest` +4 케이스.

주의: 무음 퍼팅 정확도는 학습 샘플이 풀스윙 중심이라 미검증(UNK-004). 본 감사는 소스·JVM 테스트 증거만 반영하며 전체 parity 완료나 출시 준비를 의미하지 않는다.
