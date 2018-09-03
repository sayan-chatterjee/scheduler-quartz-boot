import { Component, Input, Output, EventEmitter }                              from '@angular/core';
import { Injectable }                                                          from '@angular/core';
import { Http, Response, Headers, RequestOptions, RequestMethod, RequestOptionsArgs, URLSearchParams } from '@angular/http';
import { Subject }                                                             from 'rxjs/Rx';
import { Observable }                                                          from 'rxjs/Rx';

@Injectable()
export class SchedulerService {
    
    getJobsUrl = "http://localhost:8765/api/scheduler/jobs";
    scheduleJobUrl = "http://localhost:8765/api/scheduler/schedule";
    pauseJobUrl = "http://localhost:8765/api/scheduler/halt";
    resumeJobUrl = "http://localhost:8765/api/scheduler/recoup";
    deleteJobUrl = "http://localhost:8765/api/scheduler/delete";
    updateJobUrl = "http://localhost:8765/api/scheduler/update";
    isJobWithNamePresentUrl = "http://localhost:8765/api/scheduler/checkJobName";
    stopJobUrl = "http://localhost:8765/api/scheduler/stop";
    startJobNowUrl = "http://localhost:8765/api/scheduler/start";

    private options = new RequestOptions(
        {headers: new Headers({
            'Content-Type': 'application/json'
        })}
    );

    private getHeaders = function(jobName, endpoint, jobScheduleTime, cronExpression){
        let customHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'jobName' : jobName,
            'endpoint' : endpoint,
            'jobScheduleTime' : jobName,
            'cronExpression' : cronExpression
        };
        return new RequestOptions({headers: new Headers(customHeaders)});
        
    };

    constructor(
        private _http: Http) {
    }

    getJobs(){
        return this._http.get(this.getJobsUrl)
        .map(resData => resData.json()); 
    }

    scheduleJob(data){
        let params: URLSearchParams = new URLSearchParams();
        let keys: Array<String>;
        /*for(let key in data) {
            params.set(key, data[key]);
        }*/
        //this.options.search = params;
        for(let key in data) {
            keys.push(key);
        }
        console.log(keys);
        /*return this._http.get(this.scheduleJobUrl, this.options)
        .map(resData => resData.json()); */
        return this._http.get(this.scheduleJobUrl, this.getHeaders('','','',''))
        .map(resData => resData.json());
    }

    isJobWithNamePresent(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(this.isJobWithNamePresentUrl, this.options)
        .map(resData => resData.json()); 
    }

    pauseJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(this.pauseJobUrl, this.options)
            .map(resData => resData.json()); 
    }

    resumeJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(this.resumeJobUrl, this.options)
            .map(resData => resData.json()); 
    }

    deleteJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(this.deleteJobUrl, this.options)
            .map(resData => resData.json()); 
    }
    
    stopJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(this.stopJobUrl, this.options)
            .map(resData => resData.json()); 
    }

    startJobNow(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;
        return this._http.get(this.startJobNowUrl, this.options)
            .map(resData => resData.json()); 
    }

    updateJob(data){
        let params: URLSearchParams = new URLSearchParams();
        for(let key in data) {
            params.set(key, data[key]);
        }
        this.options.search = params;

        return this._http.get(this.updateJobUrl, this.options)
        .map(resData => resData.json()); 
    }    
}