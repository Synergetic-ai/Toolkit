export interface Format {
    /**
     * Name of the format.
     */
    name: string;

    /**
     * Function which can translate array of internal sequences to this format
     * @param sequences - array of sequence strings
     */
    write(sequences: Sequence[]): string;

    /**
     * Function which can read this format to internal sequences.
     * @param value - sequences string in this format
     */
    read(value: string): Sequence[];

    /**
     * Validate the given input and check whether it is the correct format.
     * @param value
     */
    validate(value: string): boolean;
}

/**
 * Internal sequence object
 */
export interface Sequence {
    name: string;
    seq: string;
}

export interface Operation {
    /**
     * Name of the operation.
     */
    name: string;

    /**
     * Defining function of this operation to execute.
     */
    execute(...params: any[]): string | boolean | number;
}
