import {expect} from 'chai';
import toolGetters from '@/store/modules/tools/getters.ts';
import {ToolState} from '@/store/types';

const rootState = {
    maintenanceMode: false,
    reconnecting: false,
    loading: {
        tools: false,
        toolParameters: false,
    },
    clusterWorkload: 0,
};

describe('tools/getters', () => {
    it('should return the correct sections', () => {
        const state: ToolState = {
            tools: [
                {
                    name: 'Tool1', longname: 'Tool1', section: 'Section1', description: '', version: '',
                    validationParams: {}, parameters: undefined,
                },
                {
                    name: 'Tool2', longname: 'Tool1', section: 'Section2', description: '', version: '',
                    validationParams: {}, parameters: undefined,
                },
                {
                    name: 'Tool3', longname: 'Tool1', section: 'Section2', description: '', version: '',
                    validationParams: {}, parameters: undefined,
                },
            ],
            version: '0.0.0',
        };

        const result = toolGetters.sections(state, null, rootState, null);
        expect(result).to.deep.equal(['Section1', 'Section2']);
    });
});
