#!/bin/zsh
set -euo pipefail

matchup_serial="${1:-emulator-5554}"
matchup_apk="${2:-app/build/outputs/apk/debug/app-debug.apk}"
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
  local device_png="/sdcard/hanclip-matchup-${state_id}.png"
  "$matchup_adb" -s "$matchup_serial" shell screencap -p "$device_png"
  "$matchup_adb" -s "$matchup_serial" pull "$device_png" "$matchup_output/${state_id}.png" >/dev/null
}

reset_home() {
  "$matchup_adb" -s "$matchup_serial" shell pm clear com.hanclip.android >/dev/null
  "$matchup_adb" -s "$matchup_serial" shell settings put system font_scale 1.0
  "$matchup_adb" -s "$matchup_serial" shell cmd uimode night no >/dev/null
  "$matchup_adb" -s "$matchup_serial" shell am start -W -n com.hanclip.android/.MainActivity >/dev/null
  sleep 2
}

"$matchup_adb" -s "$matchup_serial" install -r "$matchup_apk" >/dev/null
reset_home
capture_state home_default

tap_description "미디어 추가"
capture_state home_media_add_menu
"$matchup_adb" -s "$matchup_serial" shell input keyevent BACK

tap_description "HanClip 로고, 눌러 테마 변경, 길게 눌러 테마 선택"
capture_state home_theme_short_tap

reset_home
tap_description "카피라이터 설정, 길게 눌러 음악 브라우저 열기"
sleep 1
capture_state copyright_top

print "Captured Android home catalog in $matchup_output"
