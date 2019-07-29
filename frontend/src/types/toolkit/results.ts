export interface SearchAlignmentItem {
    query: any;
    agree: string;
    template: any;
}

export interface SearchHitItem {
    num: number;
}

export interface SearchHitsResponse {
    hits: SearchHitItem[];
    total: number;
    totalNoFilter: number;
    start: number;
    end: number;
}

export interface SearchAlignmentsResponse<T extends SearchAlignmentItem> {
    alignments: T[];
    total: number;
    start: number;
    end: number;
}

export interface HHompAlignmentItem extends SearchAlignmentItem {
    num: number;
    acc: string;
    name: string;
    alignedCols: number;
    probabHit: number;
    probabOMP: number;
    eval: number;
    score: number;
    identities: number;
}

export interface HHblitsAlignmentItem extends SearchAlignmentItem {
    num: number;
    acc: string;
    name: string;
    alignedCols: number;
    probab: number;
    eval: number;
    score: number;
    identities: number;
}

export interface HMMERAlignmentItem extends SearchAlignmentItem {
    num: number;
    acc: string;
    name: string;
    fullEval: number;
    eval: number;
    bitScore: number;
    hitLen: number;
}

export interface AlignmentResultResponse {
    alignments: AlignmentItem[];
    total: number;
    start: number;
    end: number;
}

export interface AlignmentItem {
    num: number;
    accession: string;
    seq: string;
}

export interface QueryItem {
    header: string;
    sequence: string;
}

export interface Quick2dResults {
    jobID: string;
    query: QueryItem;
    results: { [key: string]: string };
}

export interface PatsearchResults {
    jobID: string;
    results: {
        hits: PatsearchHit[];
        regex: string;
    };
}

export interface PatsearchHit {
    name: string;
    seq: string;
    matches: PatsearchMatch[];
}

export interface PatsearchMatch {
    i: number; // i: start index
    n: number; // n: length of match
}

export interface TprpredResults {
    desc: string[];
    hits: string[];
}

export interface HhrepidResults {
    jobID: string;
    results: {
        reptypes: HhrepidReptypes[];
    };
}

export interface HhrepidReptypes {
    pval: string;
    reps: HhrepidReptype[];
    len: number;
    typ: string;
    num: number;
}

export interface HhrepidReptype {
    prob: string;
    pval: string;
    loc: string;
    seq: string;
}

export interface ProbEvalList {
    type: string;
    vals: number[];
}

export interface HitMapResponse {
    hitAreas: HitMapItem[];
    queryLength: number;
    resubmitStart: number;
    resubmitEnd: number;
}

export interface HitMapItem {
    num: number;
    title: string;
    b: number;
    t: number;
    l: number;
    r: number;
}
