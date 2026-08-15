#!/bin/zsh
set -euo pipefail

matchup_serial="${1:-emulator-5554}"
matchup_photo_description="${2:-sample-1.png}"
matchup_font_scale="${3:-1.3}"
matchup_apk="${4:-app/build/outputs/apk/debug/app-debug.apk}"
matchup_adb="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}/platform-tools/adb"
matchup_output="reference/matchup/android/phone"
matchup_device_xml="/sdcard/hanclip-matchup-quick.xml"
matchup_local_xml="${TMPDIR:-/tmp}/hanclip-matchup-quick.xml"

if [[ "$matchup_serial" != emulator-* ]]; then
  print -u2 "Refusing to clear or change a physical device: $matchup_serial"
  exit 2
fi

if [[ ! -x "$matchup_adb" || ! -f "$matchup_apk" ]]; then
  print -u2 "adb or APK not found"
  exit 2
fi

mkdir -p "$matchup_output"

refresh_hierarchy() {
  local attempt
  for attempt in {1..10}; do
    if "$matchup_adb" -s "$matchup_serial" shell uiautomator dump "$matchup_device_xml" >/dev/null 2>&1 &&
       "$matchup_adb" -s "$matchup_serial" pull "$matchup_device_xml" "$matchup_local_xml" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  print -u2 "Unable to read the UI hierarchy"
  return 1
}

wait_for_node() {
  local attribute="$1"
  local value="$2"
  local attempt
  for attempt in {1..20}; do
    if refresh_hierarchy &&
       python3 tools/matchup/find_semantics_center.py \
         "$matchup_local_xml" "$attribute" "$value" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  print -u2 "Timed out waiting for $attribute=$value"
  return 1
}

tap_node() {
  local attribute="$1"
  local value="$2"
  local tap_x tap_y
  wait_for_node "$attribute" "$value"
  read -r tap_x tap_y < <(
    python3 tools/matchup/find_semantics_center.py "$matchup_local_xml" "$attribute" "$value"
  )
  "$matchup_adb" -s "$matchup_serial" shell input tap "$tap_x" "$tap_y"
}

capture_state() {
  local state_id="$1"
  local device_png="/sdcard/hanclip-matchup-${state_id}.png"
  "$matchup_adb" -s "$matchup_serial" shell screencap -p "$device_png"
  "$matchup_adb" -s "$matchup_serial" pull "$device_png" "$matchup_output/${state_id}.png" >/dev/null
}

"$matchup_adb" -s "$matchup_serial" install -r "$matchup_apk" >/dev/null
"$matchup_adb" -s "$matchup_serial" shell pm clear com.hanclip.android >/dev/null
"$matchup_adb" -s "$matchup_serial" shell settings put system font_scale "$matchup_font_scale"
"$matchup_adb" -s "$matchup_serial" shell cmd uimode night no >/dev/null
"$matchup_adb" -s "$matchup_serial" shell pm grant com.hanclip.android android.permission.READ_MEDIA_IMAGES
"$matchup_adb" -s "$matchup_serial" shell pm grant com.hanclip.android android.permission.READ_MEDIA_VIDEO
"$matchup_adb" -s "$matchup_serial" shell am start -W \
  -a android.intent.action.VIEW -d hanclip://quick com.hanclip.android >/dev/null
sleep 2

wait_for_node text "사진"
capture_state quick_photo_entry
tap_node content-desc "$matchup_photo_description"
capture_state quick_photo_selected
tap_node text "1개 추가"
sleep 2
capture_state "quick_duration_font_${matchup_font_scale//./_}"
"$matchup_adb" -s "$matchup_serial" shell input swipe 540 1780 540 980 450
sleep 1
capture_state "quick_duration_font_${matchup_font_scale//./_}_bottom"

print "Captured Android quick catalog in $matchup_output"
