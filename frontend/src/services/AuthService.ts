import axios from 'axios';
import {CustomJobIdValidationResult} from '@/types/toolkit/jobs';

export default class AuthService {

    public static performLogin(username: string, password: string): Promise<any> {
        return new Promise<any>(((resolve, reject) => {
            axios.post(`/api/auth/login`, {nameLogin: username, password})
                .then((response) => resolve(response.data.isValid))
                .catch(reject);
        }));
    }

    public static validateModellerKey(key: string): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.get(`/api/auth/validate/modeller?input=${key}`)
                .then((response) => resolve(response.data.isValid))
                .catch(reject);
        }));
    }

    public static validateJobId(jobId: string): Promise<CustomJobIdValidationResult> {
        return new Promise<CustomJobIdValidationResult>(((resolve, reject) => {
            axios.get(`/api/jobs/check/jobid/${jobId}/?resubmitJobID=null`) // TODO: what does resubmitJobID do?
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

}
