import {ActionTree} from 'vuex';
import ToolService from '@/services/ToolService';
import {RootState, ToolState} from '../../types';
import {Tool, ToolParameters} from '@/types/toolkit';

const actions: ActionTree<ToolState, RootState> = {
    async fetchAllTools(context) {
        context.commit('startLoading', 'tools', {root: true});
        const tools = await ToolService.fetchTools();
        context.commit('setTools', tools);
        context.commit('stopLoading', 'tools', {root: true});
    },
    async fetchToolParametersIfNotPresent(context, toolName: string) {
        const tool: Tool = context.state.tools.filter((t: Tool) => t.name === toolName)[0];
        if (tool && !tool.parameters) {
            context.commit('startLoading', 'toolParameters', {root: true});
            const parameters: ToolParameters = await ToolService.fetchToolParameters(toolName);
            context.commit('setToolParameters', {toolName, parameters});
            context.commit('stopLoading', 'toolParameters', {root: true});
        }
    },
};

export default actions;
