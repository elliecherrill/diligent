export const initialConfigs = {
    configs: {
        'config-1': { id: 'config-1', content: 'No use of == for String comparison'},
        'config-2': { id: 'config-2', content: 'Use of inheritance'},
        'config-3': { id: 'config-3', content: 'Use of interfaces'},
        'config-4': { id: 'config-4', content: 'Use of streams'},
        'config-5': { id: 'config-5', content: 'Use of camelCase (as default)'},
        'config-6': { id: 'config-6', content: 'Use of SCREAMING_SNAKE_CASE (for static final)'},
    },
    categories: {
        'category-1': { id: 'category-1', title: 'High Priority', configIds: []},
        'category-2': { id: 'category-2', title: 'Medium Priority', configIds: []},
        'category-3': { id: 'category-3', title: 'Low Priority', configIds: []},
        'category-4': { id: 'category-4', title: 'Don\'t Check', configIds: ['config-1', 'config-2', 'config-3', 'config-4', 'config-5', 'config-6']},
    },
    columnOrder: ['category-1', 'category-2', 'category-3', 'category-4']
}