import {Format, Sequence} from '@/modules/reformat/types';

export const CLUSTAL: Format = {
    name: 'Clustal',

    validate(value: string): boolean {
        return true;
    },

    read(value: string): Sequence[] {
        return [];
    },

    write(sequences: Sequence[]): string {
        return '';
    },
};
