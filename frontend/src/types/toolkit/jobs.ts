import {JobState} from '@/types/toolkit/enums';

export interface Job {
    jobID: string;
    status: JobState;
    tool: string;
    code: string;
    // toolnameLong: string;
    dateCreated?: Date;
    dateUpdated?: Date;
    dateViewed?: Date;
}
