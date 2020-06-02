export const initialConfigs = {
    configs: {
        'config-1': { id: 'config-1', content: 'No use of == (or !=) for String comparison'},
        'config-2': {id: 'config-2', content: 'Use of inheritance'},
        'config-3': {id: 'config-3', content: 'No use of inheritance'},
        'config-4': {id: 'config-4', content: 'Use of interfaces'},
        'config-5': {id: 'config-5', content: 'No use of interfaces'},
        'config-6': {id: 'config-6', content: 'Use of streams'},
        'config-7': {id: 'config-7', content: 'No use of streams'},
        'config-8': {id: 'config-8', content: 'Use of for loops'},
        'config-9': {id: 'config-9', content: 'No use of for loops'},
        'config-10': {id: 'config-10', content: 'Use of while loops'},
        'config-11': {id: 'config-11', content: 'No use of while loops'},
        'config-12': {id: 'config-12', content: 'Use of camelCase (as default)'},
        'config-13': {id: 'config-13', content: 'Use of SCREAMING_SNAKE_CASE (for static final)'},
        'config-14': {id: 'config-14', content: 'No redundant else cases'},
        'config-15': {id: 'config-15', content: 'No single character variable names'},
        'config-16': {id: 'config-16', content: 'No methods longer than 20 statements'},
        'config-17': {id: 'config-17', content: 'Minimise code duplication'},
        'config-18': {id: 'config-18', content: 'Field declarations at the top of a class'},
        'config-19': {id: 'config-19', content: 'No redundant \'this\' keywords'},
        'config-20': {id: 'config-20', content: 'Use of shorthand operation-and-assignment'},
        'config-21': {id: 'config-21', content: 'No String concatenation in a loop'},
        'config-22': {id: 'config-22', content: 'No \'if (...) { return true; } else { return false; }\''},
        'config-23': {id: 'config-23', content: 'Constructors as first methods of a class'},
    },
    categories: {
        'category-1': {id: 'category-1', title: 'High Priority', configIds: []},
        'category-2': {id: 'category-2', title: 'Medium Priority', configIds: []},
        'category-3': {id: 'category-3', title: 'Low Priority', configIds: []},
        'category-4': {
            id: 'category-4',
            title: 'Don\'t Check',
            configIds: ['config-1', 'config-2', 'config-3', 'config-4', 'config-5', 'config-6', 'config-7', 'config-8',
                'config-9', 'config-10', 'config-11', 'config-12', 'config-13', 'config-14', 'config-15', 'config-16',
                'config-17', 'config-18', 'config-19', 'config-20', 'config-21', 'config-22', 'config-23']
        },
    },
    columnOrder: ['category-4', 'category-3', 'category-2', 'category-1']
}

export const allConfigs = ['config-1', 'config-2', 'config-3', 'config-4', 'config-5', 'config-6', 'config-7', 'config-8',
    'config-9', 'config-10', 'config-11', 'config-12', 'config-13', 'config-14', 'config-15', 'config-16', 'config-17',
    'config-18', 'config-19', 'config-20', 'config-21', 'config-22', 'config-23']

export const initialPythonConfigs = {
    configs: {
        'config-1': {id: 'config-1', content: 'Use of snake_case (as default)'},
        'config-2': {id: 'config-2', content: 'No methods longer than 20 statements'},
        'config-3': {id: 'config-3', content: 'Use of \'_\' when unpacking'}
    },
    categories: {
        'category-1': {id: 'category-1', title: 'High Priority', configIds: []},
        'category-2': {id: 'category-2', title: 'Medium Priority', configIds: []},
        'category-3': {id: 'category-3', title: 'Low Priority', configIds: []},
        'category-4': {
            id: 'category-4',
            title: 'Don\'t Check',
            configIds: ['config-1', 'config-2', 'config-3']
        },
    },
    columnOrder: ['category-4', 'category-3', 'category-2', 'category-1']
}

export const allPythonConfigs = ['config-1', 'config-2', 'config-3']
