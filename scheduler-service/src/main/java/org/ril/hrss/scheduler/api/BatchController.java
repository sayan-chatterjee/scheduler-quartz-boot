package org.ril.hrss.scheduler.api;

import java.util.logging.Logger;

import javax.validation.constraints.NotNull;

import org.ril.hrss.scheduler.kafka.model.BatchDetailsMsg;
import org.ril.hrss.scheduler.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class BatchController {
    
    protected static final Logger logger = Logger.getLogger(BatchController.class.getName());
    
    @Autowired
    private BatchService batchService;
    
    @RequestMapping(value = "/batch/update", method = RequestMethod.PUT,
            produces = "application/json")
    @ApiOperation(value = "Update Batch details", response = String.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully processed request"),
            @ApiResponse(code = 201, message = "Successfully updated batch details request"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403,
                    message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404,
                    message = "The resource you were trying to reach is not found")})
    public ResponseEntity<BatchDetailsMsg> updateBatchDetails(
            @NotNull @RequestHeader("batchUuid") String batchUuid,
            @NotNull @RequestParam("batchStatus") String batchStatus) {
        logger.info("BatchController.updateBatchDetails()");
        BatchDetailsMsg batchDetailsMsg = batchService.updateBatchDetails(batchUuid, batchStatus);
        return new ResponseEntity<BatchDetailsMsg>(batchDetailsMsg, HttpStatus.OK);
        
    }
}
