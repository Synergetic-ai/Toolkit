import ControllerFunction = Mithril.ControllerFunction;


declare class JobListComponent {

    public static reloadList() : any;
    public static lastUpdatedJob : any;
    public static removeJob(param : any) : any;
    public static pushJob(param : any) : any;
    public static pushJob(param: any, bool : boolean) : any;
    public static Job(param : any) : any;
    public static selectedJobID : string;
    public static getJobIndex(jobID : string) : number;
    public static contains(jobID : string) : any;
    public static list: any;
    public static getJob : any;

    public static controller : any;
    public static view : any;
    public static jobIDsFiltered() : any;
    public static register() : any;
    public static register(item : any): any;
    public static sortList(): any;
    public static sort : any;
    public static visibleJobs() : any;
    public static scrollToJobListItem(item : any) : any;
    public static index : number;
    public static numVisibleItems : number;
    public static scrollJobList(scrollItems : any, pos: any) : any;

}

declare class FrontendAlnvizComponent {

    public static controller : any;
    public static view: any;

}

declare class FrontendReformatComponent {}

declare class JobViewComponent {

    public static controller: any;
    public static view: any;

}

declare class Backend{
    public static controller : any;
    public static view: any;
}


declare class Toolkit{
    public static currentJobID : string;
    public static controller : any;
    public static view: any;
    public static isJob : boolean;
}

declare class Index{

    public static controller : any;
    public static view: any;
}

declare class News{

    public static controller : any;
    public static view: any;
}


declare class JobManager{

    public static tableObjects : any;
    public static model : any;
    public static controller : any;
    public static view: any;

}


interface JobData {
    data : {

        jobID : any;
    }
}

declare class Job {
    jobID : string;
    toolnameLong : string;
    state : Number
}
