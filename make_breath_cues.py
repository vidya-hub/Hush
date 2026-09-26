#!/usr/bin/env python3
"""Generate quiet, distinct breathing phase cues as 16-bit mono WAVs."""
import math
import struct
import wave

RATE = 44100
PEAK = 0.24  # quiet sonification level


def envelope(t, dur, attack=0.08, release=0.18):
    if t < attack:
        return t / attack
    if t > dur - release:
        return max(0.0, (dur - t) / release)
    return 1.0


def tone(path, dur, start_hz, end_hz, wobble=0.0):
    frames = []
    n = int(RATE * dur)
    for i in range(n):
        t = i / RATE
        frac = t / dur
        hz = start_hz + (end_hz - start_hz) * frac
        if wobble:
            hz += wobble * math.sin(2 * math.pi * 5.0 * frac)
        phase = 2 * math.pi * (start_hz * t + (end_hz - start_hz) * t * frac / 2.0)
        sample = math.sin(phase) * envelope(t, dur) * PEAK
        frames.append(struct.pack("<h", int(sample * 32767)))
    with wave.open(path, "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(RATE)
        out.writeframes(b"".join(frames))
    print(f"wrote {path} ({dur:.2f}s, {start_hz:.0f}->{end_hz:.0f} Hz)")


BASE = "app/src/main/res/raw"
tone(f"{BASE}/breath_cue_in.wav", 0.55, 294, 415)     # rising: breathe in
tone(f"{BASE}/breath_cue_out.wav", 0.65, 415, 262)    # falling: breathe out
tone(f"{BASE}/breath_cue_hold.wav", 0.45, 349, 349, wobble=2.0)  # steady: hold
