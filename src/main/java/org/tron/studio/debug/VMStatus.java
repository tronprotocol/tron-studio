package org.tron.studio.debug;

import org.tron.common.runtime.vm.DataWord;
import org.tron.common.runtime.vm.OpCode;
import org.tron.common.runtime.vm.program.Memory;
import org.tron.common.runtime.vm.program.Stack;
import org.tron.common.runtime.vm.program.Storage;
import org.tron.common.runtime.vm.trace.OpActions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class VMStatus {
    public static Stack lastStack;
    public static Memory lastMemory;
    public static Storage lastStorage;

    public static class StatusItem {
        public OpCode code;
        public int deep;
        public int pc;
        public BigInteger energy;
        public OpActions actions;
        public Stack stack;
        public Memory memory;
        public Storage storage;

        public StatusItem(String contractAddress, OpCode code, int deep, int pc, BigInteger energy, OpActions actions) {
            this.code = code;
            this.deep = deep;
            this.pc = pc;
            this.energy = energy;
            this.actions = actions;
            for (OpActions.Action action : actions.getStack()) {
                if (lastStack == null) {
                    lastStack = new Stack();
                }
                stack = new Stack();
                stack.addAll(lastStack);
                switch (action.getName()) {
                    case pop:
                        stack.pop();
                        break;
                    case push:
                        stack.push(new DataWord((String) action.getParams().get("value")));
                        break;
                    case swap:
                        stack.swap(Integer.parseInt((String) action.getParams().get("from")), Integer.parseInt((String) action.getParams().get("to")));
                        break;
                }
                lastStack.clear();
                lastStack.addAll(stack);
            }
            for (OpActions.Action action : actions.getMemory()) {
                if (lastMemory == null) {
                    lastMemory = new Memory();
                }
                memory = new Memory();
                memory.getChunks().addAll(lastMemory.getChunks());

                //TODO

                lastMemory.getChunks().clear();
                lastMemory.getChunks().addAll(memory.getChunks());
            }

            for (OpActions.Action action : actions.getStorage()) {
            }
        }
    }

    private List<StatusItem> statusItemList = new ArrayList<>();

    public void addStatus(StatusItem statusItem) {
        statusItemList.add(statusItem);
    }

    public StatusItem getStatusItem(int index) {
        return statusItemList.get(index);
    }

    public int getStatusItemSize() {
        return statusItemList.size();
    }
}
