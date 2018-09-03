package org.ril.hrss.scheduler.constant;

import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.model.JobResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public enum CreateJobResponse {
    
    JOB_WITH_SAME_NAME_EXIST {
        
        public ResponseEntity<JobResponse> createResponse(JobResponse response) {
            response.setData(HRSSConstantUtil.JOB_WITH_SAME_NAME_EXIST_MSG);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
    },
    
    JOB_NAME_NOT_PRESENT {
        
        public ResponseEntity<JobResponse> createResponse(JobResponse response) {
            response.setData(HRSSConstantUtil.JOB_NAME_NOT_PRESENT_MSG);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
    },
    
    JOB_ALREADY_IN_RUNNING_STATE {
        
        public ResponseEntity<JobResponse> createResponse(JobResponse response) {
            response.setData(HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_MSG);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
    },
    
    JOB_NOT_IN_PAUSED_STATE {
        
        public ResponseEntity<JobResponse> createResponse(JobResponse response) {
            response.setData(HRSSConstantUtil.JOB_NOT_IN_PAUSED_STATE_MSG);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
    },
    
    JOB_NOT_IN_RUNNING_STATE {
        
        public ResponseEntity<JobResponse> createResponse(JobResponse response) {
            response.setData(HRSSConstantUtil.JOB_NOT_IN_RUNNING_STATE_MSG);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
    },
    
    JOB_DOESNT_EXIST {
        
        public ResponseEntity<JobResponse> createResponse(JobResponse response) {
            response.setData(HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
    },
    
    GENERIC_ERROR {
        
        public ResponseEntity<JobResponse> createResponse(JobResponse response) {
            response.setData(HRSSConstantUtil.GENERIC_ERROR_MSG);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    },
    
    SUCCESS {
        
        public ResponseEntity<JobResponse> createResponse(JobResponse response) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        
    };
    
    public abstract ResponseEntity<JobResponse> createResponse(JobResponse response);
    
}
