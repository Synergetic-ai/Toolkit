import axios from 'axios';
import {Job, SimilarJobResult, SubmissionResponse} from '@/types/toolkit/jobs';

export default class JobService {

    public static fetchJobs(): Promise<Job[]> {
        return new Promise<Job[]>((resolve, reject) => {
            axios.get('/api/jobs/')
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public static fetchJob(jobID: string): Promise<Job> {
        return new Promise<Job>(((resolve, reject) => {
            axios.get(`/results/job/${jobID}`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }

    public static getRecentJob(): Promise<Job> { // TODO should not be necessary as we have it in store
        return new Promise<Job>(((resolve, reject) => {
            axios.get(`/api/jobs/recent`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }

    public static submitJob(toolName: string, submission: any): Promise<SubmissionResponse> {
        return new Promise<SubmissionResponse>((resolve, reject) => {
            axios.post(`/api/jobs/?toolName=${toolName}`, submission)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    /**
     * Ask for delete of job. Job will get cleared over websockets as well.
     * @param jobID
     */
    public static deleteJob(jobID: string): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.delete(`/api/jobs/${jobID}`)
                .then(() => {
                    resolve();
                })
                .catch(reject);
        }));
    }

    public static getSimilarJob(jobID: string): Promise<SimilarJobResult> {
        return new Promise<SimilarJobResult>(((resolve, reject) => {
            axios.get(`/api/jobs/check/hash/${jobID}`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }

    public static startJob(jobID: string): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.get(`/api/jobs/${jobID}/start`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }

    public static suggestJobsForJobId(query: string): Promise<Job[]> {
        return new Promise<Job[]>(((resolve, reject) => {
            axios.get(`/api/jobs/suggest/${query}`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }
}
