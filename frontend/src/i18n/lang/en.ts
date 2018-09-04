export default {
    en: {
        helpContents: 'Some help text',

        index: {
            welcomeTitle: 'Welcome to the Bioinformatics Toolkit',
            welcomeBody: 'of the Max Planck Institute for Developmental Biology, Tübingen, Germany.',
            loadBarLabel: 'Cluster workload: {load}%',
            searchPlaceholder: 'Enter a job ID or a tool name',
        },
        jobList: {
            sortColumns: {
                id: 'ID',
                date: 'Date',
                tool: 'Tool',
            },
        },
        tools: {
            sections: {
                Search: 'Search',
                Alignment: 'Alignment',
            },
            parameters: {
                alignTwoSeqToggle: 'Align two sequences or MSAs',
                select: {
                    singlePlaceholder: 'Select one',
                    multiplePlaceholder: 'Select options',
                    maxElementsSelected: 'Max. elements selected',
                },
            },
            validation: {
                proteinFasta: 'Protein FASTA',
                invalidCharacters: 'Invalid Characters!',
                sameLength: 'Invalid MSA! Sequences should have the same length.',
                uniqueIDs: 'Identifiers are not unique!',
            },
        },
        helpModals: {
            names: {
                help: 'Help',
                faq: 'FAQ',
                privacy: 'Privacy Policy',
                imprint: 'Imprint',
                contact: 'Contact Us',
                cite: 'Cite Us',
                updates: 'Recent Updates',
            },
            titles: {
                help: 'Welcome to the MPI Bioinformatics Toolkit',
                faq: 'FAQ',
                privacy: 'Privacy Policy',
                imprint: 'Imprint',
                contact: 'Contact Us',
                cite: 'Cite Us',
                updates: 'Recent Updates',
            },
        },
        copyright: '© 2008-{currentYear}, Dept. of Protein Evolution, ' +
            'Max Planck Institute for Developmental Biology, Tübingen',
    },
};
