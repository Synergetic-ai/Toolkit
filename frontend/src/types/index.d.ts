declare module 'vue-multiselect';
declare module 'velocity-animate';
declare module 'vue-particles';
declare module 'msa' {
    interface MSA {
        render: () => void;
        addView: (name: string, menu: any) => void;
    }

    const msa: {
        menu: {
            defaultmenu: {
                new(options: any): void;
            },
        };
        io: any;
        msa: {
            new(options: any): MSA;
        }
    };
    export = msa;
}
