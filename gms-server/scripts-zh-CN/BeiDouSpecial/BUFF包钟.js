var status = 0;

function start() {
    status = 0;
    cm.sendSimple(
        "请选择：\r\n" +
        "#L1#开启自动BUFF（按时间收费）#l\r\n" +
        "#L0#关闭自动BUFF#l"
    );
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
        return;
    }

    if (status == 0) {
        if (selection == 1) {
            cm.playerAutoBuff(true);
            cm.sendOk("自动BUFF已开启。");
        } else {
            cm.playerAutoBuff(false);
            cm.sendOk("自动BUFF已关闭。");
        }
        cm.dispose();
    }
}