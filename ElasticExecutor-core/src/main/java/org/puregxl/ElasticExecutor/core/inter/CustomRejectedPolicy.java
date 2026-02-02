package org.puregxl.ElasticExecutor.core.inter;

import java.util.concurrent.RejectedExecutionHandler;

public interface CustomRejectedPolicy {


    String getName();


    RejectedExecutionHandler generatePolicy();
}
