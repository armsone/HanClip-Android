#!/bin/zsh
set -euo pipefail

matchup_serial="${1:-emulator-5554}"
matchup_apk="${2:-app/build/outputs/apk/debug/app-debug.apk}"
matchup_revision="${3:-}"
matchup_adb="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}/platform-tools/adb"
matchup_output="reference/matchup/android/phone"
matchup_device_xml="/sdcard/hanclip-matchup-window.xml"
matchup_local_xml="${TMPDIR:-/tmp}/hanclip-matchup-window.xml"

if [[ "$matchup_serial" != emulator-* ]]; then
  print -u2 "Refusing to clear or change a physical device: $matchup_serial"
  exit 2
fi

if [[ ! -x "$matchup_adb" || ! -f "$matchup_apk" ]]; then
  print -u2 "adb or APK not found"
  exit 2
fi

mkdir -p "$matchup_output"

tap_description() {
  local description="$1"
  local tap_x tap_y
  "$matchup_adb" -s "$matchup_serial" shell uiautomator dump "$matchup_device_xml" >/dev/null
  "$matchup_adb" -s "$matchup_serial" pull "$matchup_device_xml" "$matchup_local_xml" >/dev/null
  read -r tap_x tap_y < <(python3 tools/matchup/find_semantics_center.py "$matchup_local_xml" "$description")
  "$matchup_adb" -s "$matchup_serial" shell input tap "$tap_x" "$tap_y"
}

capture_state() {
  local state_id="$1"
  local output_id="$state_id"
  if [[ -n "$matchup_revision" ]]; then
    output_id="${state_id}_${matchup_revision}"
  fi
  local device_png="/sdcard/hanclip-matchup-${state_id}.png"
  "$matchup_adb" -s "$matchup_serial" shell screencap -p "$device_png"
  "$matchup_adb" -s "$matchup_serial" pull "$device_png" "$matchup_output/${output_id}.png" >/dev/null
}

wait_for_description() {
  local description="$1"
  local attempt
  for attempt in {1..20}; do
    if "$matchup_adb" -s "$matchup_serial" shell uiautomator dump "$matchup_device_xml" >/dev/null 2>&1 &&
       "$matchup_adb" -s "$matchup_serial" pull "$matchup_device_xml" "$matchup_local_xml" >/dev/null 2>&1 &&
       python3 tools/matchup/find_semantics_center.py "$matchup_local_xml" "$description" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  print -u2 "Timed out waiting for content-desc=$description"
  return 1
}

reset_home() {
  "$matchup_adb" -s "$matchup_serial" shell pm clear com.hanclip.android >/dev/null
  "$matchup_adb" -s "$matchup_serial" shell settings put system font_scale 1.0
  "$matchup_adb" -s "$matchup_serial" shell cmd uimode night no >/dev/null
  "$matchup_adb" -s "$matchup_serial" shell input keyevent BACK >/dev/null 2>&1 || true
  "$matchup_adb" -s "$matchup_serial" shell am force-stop com.hanclip.android
  "$matchup_adb" -s "$matchup_serial" shell am start -W -n com.hanclip.android/.MainActivity >/dev/null
  wait_for_description "미디어 추가"
}

"$matchup_adb" -s "$matchup_serial" install -r "$matchup_apk" >/dev/null
reset_home
capture_state home_empty_default

tap_description "미디어 추가"
sleep 1
capture_state media_menu_open
"$matchup_adb" -s "$matchup_serial" shell input keyevent BACK

tap_description "HanClip 로고, 눌러 테마 변경, 길게 눌러 테마 선택"
sleep 1
capture_state theme_notice

reset_home
tap_description "HanClip 로고, 눌러 테마 변경, 길게 눌러 테마 선택"
sleep 1
tap_description "HanClip 로고, 눌러 테마 변경, 길게 눌러 테마 선택"
sleep 4
capture_state home_empty_dark
tap_description "카피라이터 설정, 길게 눌러 음악 브라우저 열기"
sleep 1
capture_state copyright_collapsed_dark
tap_description "워터마크 설정 펼치기"
sleep 1
capture_state copyright_expanded_dark

print "Captured Android home catalog in $matchup_output"
