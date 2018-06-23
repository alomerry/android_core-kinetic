package mo;

/**
 * Created by wu1ji on 2018/1/7.
 */

public interface ConstInt {
    //唤醒成功
    int WakeUp_State_success = 0x01;
    //未唤醒
    int WakeUp_State_waitForWake = 0x02;

    int PRINT = 0;
    int UI_CHANGE_INPUT_TEXT_SELECTION = 1;
    int UI_CHANGE_SYNTHES_TEXT_SELECTION = 2;

    int INIT_SUCCESS = 2;
}
