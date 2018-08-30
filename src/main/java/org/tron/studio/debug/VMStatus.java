package org.tron.studio.debug;

import org.tron.common.runtime.vm.OpCode;
import org.tron.common.runtime.vm.trace.OpActions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class VMStatus {

    public static class StatusItem {
        public OpCode code;
        public int deep;
        public int pc;
        public BigInteger energy;
        public OpActions actions;


        public StatusItem(OpCode code, int deep, int pc, BigInteger energy, OpActions actions) {
            this.code = code;
            this.deep = deep;
            this.pc = pc;
            this.energy = energy;
            this.actions = actions;
        }
    }

    private List<StatusItem> statusItemList = new ArrayList<>();

    public void addStatus(StatusItem statusItem) {
        statusItemList.add(statusItem);
    }
}
