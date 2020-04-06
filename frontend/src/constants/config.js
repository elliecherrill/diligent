export const initialConfigs = {
    configs: {
        // TODO: 'config-1': { id: 'config-1', content: 'No use of == for String comparison'},
        'config-2': {id: 'config-2', content: 'Use of inheritance'},
        'config-3': {id: 'config-3', content: 'Use of interfaces'},
        // TODO: 'config-4': { id: 'config-4', content: 'Use of streams'},
        // TODO: 'config-5': { id: 'config-5', content: 'Use of for loops'},
        // TODO: 'config-6': { id: 'config-6', content: 'Use of while loops'},
        'config-7': {id: 'config-7', content: 'Use of camelCase (as default)'},
        'config-8': {id: 'config-8', content: 'Use of SCREAMING_SNAKE_CASE (for static final)'},
        'config-9': {id: 'config-9', content: 'No redundant else cases'},
        'config-10': {id: 'config-10', content: 'No single character variable names'},
        'config-11': {id: 'config-11', content: 'No methods longer than 20 statements'},
        'config-12': {id: 'config-12', content: 'No clones'},
    },
    categories: {
        'category-1': {id: 'category-1', title: 'High Priority', configIds: []},
        'category-2': {id: 'category-2', title: 'Medium Priority', configIds: []},
        'category-3': {id: 'category-3', title: 'Low Priority', configIds: []},
        'category-4': {
            id: 'category-4',
            title: 'Don\'t Check',
            configIds: ['config-2', 'config-3', 'config-7', 'config-8', 'config-9', 'config-10', 'config-11', 'config-12']
        }, //TODO: 'config-1', 'config-4', 'config-5', 'config-6',
    },
    columnOrder: ['category-4', 'category-3', 'category-2', 'category-1']
}
