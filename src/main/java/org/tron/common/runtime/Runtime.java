package org.tron.common.runtime;

import org.tron.common.runtime.vm.program.InternalTransaction.TrxType;
import org.tron.common.runtime.vm.program.ProgramResult;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.VMIllegalException;


public interface Runtime {

  boolean isCallConstant() throws ContractValidateException;

<<<<<<< HEAD
    if (config.vmTrace() && program != null && blockCap != null && !blockCap.getInstance().getBlockHeader().getWitnessSignature().isEmpty()) {
      String traceContent = program.getTrace()
          .result(result.getHReturn())
          .error(result.getException())
          .toString();
=======
  void execute() throws ContractValidateException, ContractExeException, VMIllegalException;
>>>>>>> java-tron/develop

  void go();

<<<<<<< HEAD
      saveProgramTraceFile(config, trx, traceContent);
    }
=======
  TrxType getTrxType();
>>>>>>> java-tron/develop

  void finalization();

  ProgramResult getResult();

  String getRuntimeError();
}
