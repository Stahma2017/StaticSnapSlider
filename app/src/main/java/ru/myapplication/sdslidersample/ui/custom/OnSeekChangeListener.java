package ru.myapplication.sdslidersample.ui.custom;

public interface OnSeekChangeListener {
    void onSeeking(SeekParams seekParams);
    void onStartTrackingTouch(IndicatorSeekBar seekBar);
    void onStopTrackingTouch(IndicatorSeekBar seekBar);
}
