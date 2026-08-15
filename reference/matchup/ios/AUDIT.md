# HanClip iOS Matchup Reference Audit

## Evidence decision

- Source of truth: current dirty iOS working tree at `HEAD 31e60ec5`, app `1.0.1 (3.11.47)`.
- Stable paired-ready captures: `HOME.empty.automatic-light`, `MEDIA_MENU.open.automatic-light`, `HOME.empty.dark`, `COPYRIGHT.collapsed.dark`, `COPYRIGHT.expanded.dark`.
- Partial captures: both `BROWSER` captures. The app-owned chrome/panel is valid evidence, but the embedded web page is network-owned and unstable.
- Not captured in this pass: theme notice/panel, populated fixtures, media permission/selection, editor and downstream production flows. The pass stopped when another task booted the shared CoreSimulator device; no parity-complete claim is allowed.
- Exact source values may be used for tokens and geometry. Screenshot-only visual estimates must not replace source values.

## Primary paired profile correction

The fresh iOS state is **theme mode `automatic` under system light**, not the explicit `Light Mode` theme.

- Automatic/system-light: primary `#072931`, secondary `#007E81`, light system background.
- Explicit Light Mode is a separate theme state: primary `#002228`, secondary `#005C60`, background `#FAFEFD`.
- Android must keep these states separate when creating paired captures.

## Current HOME exact source contract

| Item | iOS current value |
|---|---|
| Grid | 3 columns at default Dynamic Type; 2 at `xxxLarge`; 1 at accessibility sizes |
| Grid spacing | horizontal `8pt`, vertical `10pt`, outer horizontal inset `14pt` |
| Card height | `74 + scaled(72 relativeTo: body)`; default `146pt` |
| Card padding | horizontal `5pt`, vertical `12pt` |
| Vertical layout | icon, title, subtitle occupy three equal flexible zones |
| Icon surface | `40×40pt`, radius `8pt`; SF Symbol `19pt bold`; AiShot asset `25×25pt` |
| Title | system subheadline, semibold, default `15pt`, max 2 lines |
| Subtitle | scaled footnote baseline `10.4pt`, max 2 lines, centered |
| Card chrome | radius `8pt`, stroke `1pt`, shadow radius `7pt`, y `4pt` |
| Touch/semantics | entire card button; accessibility label is title, hint is subtitle |

Source: `HanClip/Views/EditorView.swift` (`homeMoviePresetGrid`, `homePresetColumnCount`, `homePresetCardHeight`, `homeQuickStartButton`) and `HanClip/App/HanClipApp.swift` (`HanClipTheme`, `HanClipTypography`).

## Atomic paired matrix — captured states

| Route/state ID | Element/anatomy | Dimension/action | Fixture/profile | iOS exact reference | Android observed | Difference | Required action | Evidence/confidence | Status/exception proof |
|---|---|---|---|---|---|---|---|---|---|
| HOME.empty.automatic-light | page/background | theme/color | fresh-empty-v1, automatic/system-light, font 1.0 | `home_empty_default.png`; automatic token path | paired capture pending | pending | use automatic palette, not explicit Light Mode | source+PNG / High | 확인 필요 |
| HOME.empty.automatic-light | header/logo | bounds/placement | same | LogoMarkV2 + `HanClip`, leading; i and media-add trailing | pending | pending | decompose mark/text/i/add and compare each bound | PNG / High | 확인 필요 |
| HOME.empty.automatic-light | preset grid | geometry | same | 3×2, inset 14, gaps 8/10, card 146 high | pending | pending | source-exact Compose geometry then paired pixel review | source+PNG / High | 확인 필요 |
| HOME.empty.automatic-light | preset card/icon | artwork | same | 40 square/r8; SF 19 bold or AiShot asset25 | pending | pending | same asset/path, tint, shadow and bounds | source+PNG / High | 확인 필요 |
| HOME.empty.automatic-light | preset card/title | typography/content | same | exact 6 titles, subheadline semibold 15, max2 | pending | pending | exact Unicode, weight, baseline, wrap | source+PNG / High | 확인 필요 |
| HOME.empty.automatic-light | preset card/supporting text | typography/content | same | exact 6 subtitles, scaled 10.4, max2 | pending | pending | exact text and no premature ellipsis | source+PNG / High | 확인 필요 |
| HOME.empty.automatic-light | movie list empty | hierarchy/state | same | `0/10`, `영화 목록`, two skeleton rows | pending | pending | match count/header/skeleton placement and opacity | PNG / High | 확인 필요 |
| HOME.empty.automatic-light | collection empty | hierarchy/state | same | `0/30`, `컬렉션`, ADD A FILM follows below fold | pending | pending | same order and scroll boundary | PNG / High | 확인 필요 |
| MEDIA_MENU.open.automatic-light | menu container | shape/placement | fresh-empty-v1 | trailing popup under media-add | pending | pending | match popup bounds, radius, fill, anchor | PNG / High | 확인 필요 |
| MEDIA_MENU.open.automatic-light | menu actions | content/icon/action | same | `AiShot`, `사진`, `달력`, `파일` in that order | pending | pending | exact label, icon asset/SF path, route | AX+PNG / High | 확인 필요 |
| HOME.empty.dark | full HOME | theme/color | fresh-empty-v1, dark | `home_empty_dark.png`; Night Slate tokens | pending | pending | capture same dark state and compare all app pixels | source+PNG / High | 확인 필요 |
| COPYRIGHT.collapsed.dark | header | controls/content | fresh-empty-v1, dark | HanClip logo, `카피라이터 설정`, close/reset controls | pending | pending | exact icon/placement/text | AX+PNG / High | 확인 필요 |
| COPYRIGHT.collapsed.dark | watermark section | collapsed state | same | `워터마크`, `구매 옵션`, collapsed chevron | pending | pending | same default state and action | AX+PNG / High | 확인 필요 |
| COPYRIGHT.collapsed.dark | sleep setting | content/state | same | `화면 꺼짐 방지`, three-state control, helper text | pending | pending | compare states and persistence trace | AX+PNG / High | 확인 필요 |
| COPYRIGHT.expanded.dark | purchase panel | expanded state/content | same | permanent/year/month cards and exact prices shown by StoreKit fixture | pending | pending | capture same product availability; separate content-state if prices differ | AX+PNG / Medium | 확인 필요 |
| BROWSER.default.dark | browser chrome | geometry/action | fresh-empty-v1, dark | close, URL, go, reload, bookmark toolbar | pending | pending | compare app-owned toolbar only | source+PNG / High | 확인 필요 |
| BROWSER.default.dark | web body | external content | network-dependent | blank/loading then remote page | pending | variable | exclude only exact embedded web rect with documented mask | runtime / Low | 확인 필요; not yet a forced exception |
| BROWSER.favorites.dark | favorites panel | anatomy/content | built-in favorites | title/edit + 3 default rows + trash buttons | pending | pending | exact title, URL, favicon, home and delete affordances | AX+PNG / High | 확인 필요 |
| BROWSER.favorites.dark | external page/cookie UI | external content | network-dependent | remote Pixabay content | pending | variable | mask exact web-owned region, not app panel | runtime / Low | 확인 필요; mask unmeasured |

## Required route/state inventory — capture status

`captured` means an original 1206×2622 PNG and SHA-256 exist. It does not mean Android parity has passed.

| Group | Required states | Current iOS evidence |
|---|---|---|
| HOME | empty, populated, shared, busy | empty captured; others 확인 필요 |
| MEDIA_MENU | open | captured |
| THEME | automatic/light/dark/custom, notice, panel, reorder | automatic-light + dark HOME captured; notice/panel/reorder 확인 필요 |
| COPYRIGHT | collapsed, expanded, default-on/off/persisted | collapsed/expanded dark captured; on/off/persistence trace 확인 필요 |
| PHOTO | entry, selected, drag, reverse, loading, empty, filter | 확인 필요 |
| CALENDAR | month, today, selected | 확인 필요 |
| QUICK_DURATION | default, settings-return, font1.3 | 확인 필요 |
| EDITOR | empty, populated, expanded | 확인 필요 |
| CLIP_TRIM | photo, video, delete-confirm | 확인 필요 |
| TEXT | default, custom, font | 확인 필요 |
| MUSIC | none, sample, file | 확인 필요 |
| BROWSER | default, favorites, download, error | default/favorites partial; download/error 확인 필요 |
| ENDING | off, themes, duration | 확인 필요 |
| GENERATION | progress, cancel, error | 확인 필요 |
| PREVIEW | paused, playing, fullscreen | 확인 필요 |
| RELEASE | options, progress, error | 확인 필요 |
| COLLECTION | 0, 1, 29, 30, progress | HOME empty collection partially visible; distinct states 확인 필요 |
| COLLECTION_PLAYER | portrait, landscape, zoom | 확인 필요 |
| COLLECTION_POSTER_AI | loading, candidates, error | 확인 필요 |
| COLLECTION_COMPRESS | options, progress, cancel | 확인 필요 |
| AISHOT | permission, ready, capture, save | 확인 필요 |
| PERMISSION_ALERT | denied, permanent, recovered | 확인 필요 |

## Reproduction commands

The captured temporary device was intentionally deleted after use. The profile can be regenerated without touching a shared simulator:

```bash
xcrun simctl create HanClipMatchup-iPhone17Pro-YYYYMMDD \
  com.apple.CoreSimulator.SimDeviceType.iPhone-17-Pro \
  com.apple.CoreSimulator.SimRuntime.iOS-26-5

xcodebuild -project HanClip.xcodeproj -scheme HanClip \
  -configuration Debug -destination 'platform=iOS Simulator,id=<UDID>' \
  -derivedDataPath /private/tmp/hanclip-matchup-ios.<task> \
  CODE_SIGNING_ALLOWED=NO build

xcrun simctl install <UDID> \
  /private/tmp/hanclip-matchup-ios.<task>/Build/Products/Debug-iphonesimulator/HanClip.app
SIMCTL_CHILD_TZ=Asia/Seoul xcrun simctl launch <UDID> com.intosharp.hanclip
xcrun simctl io <UDID> screenshot --type=png <state-id>.png
shasum -a 256 <state-id>.png
```

The first five stable captures are suitable for immediate Android paired capture. Full parity remains unverified until all inventory rows have stable pairs and behavioral traces.
