import React from 'react'
import {initialConfigs} from '../../../constants/config'
import Column from './Column'
import {DragDropContext} from 'react-beautiful-dnd'
import styled from 'styled-components'
import colours from '../../../constants/colours'
import {
    Button,
    Slide,
    TextField,
    Tooltip,
    IconButton,
    MenuItem,
    Menu, Fade,
    Paper,
    InputBase,
    Divider
} from '@material-ui/core'
import * as API from '../../../api'
import routes from '../../../constants/routes'
import {Redirect} from 'react-router-dom'
import {
    NoteAdd as AddDetailIcon,
    Clear as DeleteIcon,
    Search as SearchIcon
} from '@material-ui/icons'
import Alert from '../viewConfigs/Alert'

const Container = styled.div`
    display: flex;
`

class NewConfig extends React.Component {
    state = {
        ...initialConfigs,
        title: '',

        goToHome: false,
        anchorEl: null,

        titleError: false,
        emptyConfigError: false,
        inverseError: false,

        addCourseCode: false,
        courseCode: '',
        addExerciseNum: false,
        exerciseNum: '',

        duplicateTitle: false,

        searchBy: '',
        searching: false,
        searchResults: 0
    }

    moveToTop = () => {
        const newColumns = []
        let results = 0
        for (let i = 0; i < this.state.columnOrder.length; i++) {
            const column = this.state.categories[this.state.columnOrder[i]]
            const searchedConfigs = []

            for (let j = 0; j < column.configIds.length; j++) {
                const config = this.state.configs[column.configIds[j]]
                if (config.content.toLowerCase().includes(this.state.searchBy)) {
                    searchedConfigs.push(config.id)
                    results++
                }
            }

            if (searchedConfigs.length === 0) {
                newColumns.push(column)
                continue
            }

            const newConfigIds = Array.from(column.configIds)
            let insertAt = 0

            for (let k = 0; k < searchedConfigs.length; k++) {
                const currIndex = newConfigIds.indexOf(searchedConfigs[k])
                if (currIndex === insertAt) {
                    insertAt++
                    continue
                }
                newConfigIds.splice(currIndex, 1)
                newConfigIds.splice(insertAt, 0, searchedConfigs[k])
                insertAt++
            }

            const newColumn = {
                ...column,
                configIds: newConfigIds
            }

            newColumns.push(newColumn)
        }

        const newState = {
            ...this.state,
            searching: true,
            searchResults: results,
            categories: {
                ...this.state.categories,
                [newColumns[0].id]: newColumns[0],
                [newColumns[1].id]: newColumns[1],
                [newColumns[2].id]: newColumns[2],
                [newColumns[3].id]: newColumns[3],
            }
        }

        this.setState(newState)
    }

    onDragEnd = result => {
        const {destination, source, draggableId} = result

        if (!destination) {
            return
        }

        if (destination.droppableId === source.droppableId &&
            destination.index === source.index) {
            return
        }

        const start = this.state.categories[source.droppableId]
        const finish = this.state.categories[destination.droppableId]

        if (start === finish) {
            const newConfigIds = Array.from(start.configIds)

            newConfigIds.splice(source.index, 1)
            newConfigIds.splice(destination.index, 0, draggableId)

            const newColumn = {
                ...start,
                configIds: newConfigIds
            }

            const newState = {
                ...this.state,
                categories: {
                    ...this.state.categories,
                    [newColumn.id]: newColumn
                }
            }

            this.setState(newState)
            return
        }

        const startConfigIds = Array.from(start.configIds)
        startConfigIds.splice(source.index, 1)
        const newStart = {
            ...start,
            configIds: startConfigIds
        }

        const finishConfigIds = Array.from(finish.configIds)
        finishConfigIds.splice(destination.index, 0, draggableId)
        const newFinish = {
            ...finish,
            configIds: finishConfigIds
        }

        const newState = {
            ...this.state,
            categories: {
                ...this.state.categories,
                [newStart.id]: newStart,
                [newFinish.id]: newFinish
            }
        }

        this.setState(newState)
    }

    getHighPriorityChecks = () => {
        return this.state.categories['category-1'].configIds
    }

    getMediumPriorityChecks = () => {
        return this.state.categories['category-2'].configIds
    }

    getLowPriorityChecks = () => {
        return this.state.categories['category-3'].configIds
    }

    inverseChecksSelected = () => {
        const unusedChecks = this.state.categories['category-4'].configIds
        if (!unusedChecks.includes('config-2') && !unusedChecks.includes('config-3')) {
            return true
        }

        if (!unusedChecks.includes('config-4') && !unusedChecks.includes('config-5')) {
            return true
        }

        if (!unusedChecks.includes('config-6') && !unusedChecks.includes('config-7')) {
            return true
        }

        if (!unusedChecks.includes('config-8') && !unusedChecks.includes('config-9')) {
            return true
        }

        if (!unusedChecks.includes('config-10') && !unusedChecks.includes('config-11')) {
            return true
        }

        return false
    }

    getInverseChecks = () => {
        const unusedChecks = this.state.categories['category-4'].configIds
        if (!unusedChecks.includes('config-2') && !unusedChecks.includes('config-3')) {
            return [initialConfigs.configs['config-2'].content, initialConfigs.configs['config-3'].content]
        }

        if (!unusedChecks.includes('config-4') && !unusedChecks.includes('config-5')) {
            return [initialConfigs.configs['config-4'].content, initialConfigs.configs['config-5'].content]
        }

        if (!unusedChecks.includes('config-6') && !unusedChecks.includes('config-7')) {
            return [initialConfigs.configs['config-6'].content, initialConfigs.configs['config-7'].content]
        }

        if (!unusedChecks.includes('config-8') && !unusedChecks.includes('config-9')) {
            return [initialConfigs.configs['config-8'].content, initialConfigs.configs['config-9'].content]
        }

        if (!unusedChecks.includes('config-10') && !unusedChecks.includes('config-11')) {
            return [initialConfigs.configs['config-10'].content, initialConfigs.configs['config-11'].content]
        }

        return []
    }

    isValid = () => {
        if (this.state.title === '') {
            const newState = {
                ...this.state,
                titleError: true
            }
            this.setState(newState)
            return false
        }

        if (this.getHighPriorityChecks().length === 0 &&
            this.getMediumPriorityChecks().length === 0 &&
            this.getLowPriorityChecks().length === 0) {
            const newState = {
                ...this.state,
                emptyConfigError: true
            }
            this.setState(newState)
            return false
        }

        if (this.inverseChecksSelected()) {
            const newState = {
                ...this.state,
                inverseError: true
            }
            this.setState(newState)
            return false
        }

        return true
    }

    handleMenuClose = () => {
        this.setState({...this.state, anchorEl: null})
    }

    addCourseCode = () => {
        this.setState({...this.state, anchorEl: null, addCourseCode: true})
    }

    addExerciseNum = () => {
        this.setState({...this.state, anchorEl: null, addExerciseNum: true})
    }

    saveConfig = () => {
        const courseCode = this.state.addCourseCode ? this.state.courseCode : null
        const exerciseNum = this.state.addExerciseNum ? this.state.exerciseNum : null

        API.create_new_config(
            this.state.title,
            this.getHighPriorityChecks(),
            this.getMediumPriorityChecks(),
            this.getLowPriorityChecks(),
            courseCode,
            exerciseNum
        ).then(() => {
            const newState = {
                ...this.state,
                goToHome: true
            }
            this.setState(newState)
        }).catch(() => {
            const newState = {
                ...this.state,
                duplicateTitle: true
            }
            this.setState(newState)
        })
    }

    render() {
        document.body.style.backgroundColor = colours.PRIMARY
        return (
            <div>
                <Slide direction='down' in={true} mountOnEnter unmountOnExit>
                    <div style={{paddingLeft: '5%', paddingRight: '5%'}}>
                        <Paper component='form' style={{display: 'flex', margin: '0.5%'}}>
                            <InputBase
                                style={{marginLeft: '1%', flex: '1'}}
                                placeholder='Search'
                                value={this.state.searchBy}
                                onChange={(e) => {
                                    const newState = {
                                        ...this.state,
                                        searchBy: e.target.value.toLowerCase(),
                                        searching: false
                                    }
                                    this.setState(newState)
                                }}
                            />
                            {this.state.searching &&
                            <Divider orientation='vertical' flexItem style={{margin: '0.5%'}}/>
                            }
                            {this.state.searching &&
                            <div style={{display: 'flex', flexDirection: 'column', justifyContent: 'center', color: 'gray', marginLeft: '0.5%'}}>
                                <p>{this.state.searchResults} search results</p>
                            </div>
                            }
                            {this.state.searching ?
                                <IconButton
                                    style={{padding: '1%'}}
                                    onClick={() => {
                                        const newState = {
                                            ...this.state,
                                            searching: false,
                                            searchBy: '',
                                        }
                                        this.setState(newState)
                                    }}
                                >
                                    <DeleteIcon/>
                                </IconButton>
                                :
                                <IconButton
                                    style={{padding: '1%'}}
                                    onClick={this.moveToTop}
                                >
                                    <SearchIcon/>
                                </IconButton>

                            }
                        </Paper>
                        <DragDropContext
                            onDragEnd={this.onDragEnd}
                        >
                            <Container>
                                {this.state.columnOrder.map((columnId) => {
                                    const column = this.state.categories[columnId]
                                    const configs = column.configIds.map(configId => this.state.configs[configId])

                                    return <Column
                                        key={column.id}
                                        column={column}
                                        configs={configs}
                                        searching={this.state.searching}
                                        searchText={this.state.searchBy}
                                    />
                                })}
                            </Container>
                        </DragDropContext>
                    </div>
                </Slide>
                <Slide direction='up' in={true} mountOnEnter unmountOnExit>
                    <div style={{backgroundColor: 'white', padding: '3%', marginTop: '5%'}}>
                        <div style={{display: 'flex', flexDirection: 'column'}}>
                            <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'center'}}>
                                <TextField
                                    required
                                    label='Configuration Name'
                                    variant='outlined'
                                    color='primary'
                                    autoComplete='off'
                                    style={{marginLeft: '5%'}}
                                    id={'title-input'}
                                    error={this.state.titleError}
                                    helperText={this.state.titleError ? 'Name of Configuration cannot be empty.' : ''}
                                    onChange={(e) => {
                                        const newState = {
                                            ...this.state,
                                            title: e.target.value,
                                            titleError: false
                                        }
                                        this.setState(newState)
                                    }}
                                />
                                <div style={{marginLeft: '2%'}}>
                                    <Tooltip title='Add More Details'>
                                        <IconButton
                                            color='primary'
                                            disabled={this.state.addExerciseNum && this.state.addCourseCode}
                                            onClick={(e) => this.setState({...this.state, anchorEl: e.currentTarget})}
                                        >
                                            <AddDetailIcon/>
                                        </IconButton>
                                    </Tooltip>
                                    {(!this.state.addExerciseNum || !this.state.addCourseCode) &&
                                    <Menu
                                        id='simple-menu'
                                        anchorEl={this.state.anchorEl}
                                        keepMounted
                                        open={Boolean(this.state.anchorEl)}
                                        onClose={this.handleMenuClose}
                                    >
                                        {!this.state.addCourseCode &&
                                        <MenuItem onClick={this.addCourseCode}>Add Course Code</MenuItem>}
                                        {!this.state.addExerciseNum &&
                                        <MenuItem onClick={this.addExerciseNum}>Add Exercise Number</MenuItem>}
                                    </Menu>
                                    }
                                </div>
                            </div>
                            {(this.state.addCourseCode || this.state.addExerciseNum) &&
                            <div style={{
                                display: 'flex',
                                flexDirection: 'row',
                                justifyContent: 'center',
                                marginTop: '2%'
                            }}>
                                <Fade in={this.state.addCourseCode}>
                                    <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'center'}}>
                                        <TextField
                                            label='Course Code'
                                            variant='outlined'
                                            color='primary'
                                            autoComplete='off'
                                            style={{marginLeft: '1%', marginRight: '1%'}}
                                            id={'course-code-input'}
                                            helperText={'For Example: CO161'}
                                            onChange={(e) => {
                                                this.setState({...this.state, courseCode: e.target.value})
                                            }}
                                        />
                                        <div>
                                            <IconButton
                                                color='primary'
                                                onClick={() => this.setState({...this.state, addCourseCode: false})}
                                            >
                                                <DeleteIcon/>
                                            </IconButton>
                                        </div>
                                    </div>
                                </Fade>
                                <Fade in={this.state.addExerciseNum}>
                                    <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'center'}}>
                                        <TextField
                                            label='Exercise Number'
                                            variant='outlined'
                                            color='primary'
                                            autoComplete='off'
                                            style={{marginLeft: '1%', marginRight: '1%'}}
                                            id={'ex-num-input'}
                                            helperText={'For Example: 17 Java Spreadsheet'}
                                            onChange={(e) => {
                                                this.setState({...this.state, exerciseNum: e.target.value})
                                            }}
                                        />
                                        <div>
                                            <IconButton
                                                color='primary'
                                                onClick={() => this.setState({...this.state, addExerciseNum: false})}
                                            >
                                                <DeleteIcon/>
                                            </IconButton>
                                        </div>
                                    </div>
                                </Fade>
                            </div>
                            }

                            <div style={{display: 'flex', justifyContent: 'center', marginTop: '2%'}}>
                                <Button
                                    variant='contained'
                                    color='primary'
                                    onClick={() => {
                                        if (this.isValid()) {
                                            this.saveConfig()
                                        }
                                    }}
                                >
                                    Save Configuration
                                </Button>
                            </div>

                            {this.state.goToHome &&
                            <Redirect push to={{
                                pathname: routes.HOME,
                                state: {title: this.state.title, new: true, edit: false}
                            }}/>}

                            <Alert
                                title={'Conflict Configuration'}
                                content={'One of your configurations is already called \'' + this.state.title + '\'. Please choose a unique configuration name.'}
                                actions={[
                                    {
                                        title: 'OK',
                                        action: (() => this.setState({...this.state, duplicateTitle: false}))
                                    },
                                ]}
                                open={this.state.duplicateTitle}
                            />

                            <Alert
                                title={'Empty Configuration'}
                                content={'You have not selected any checks. Please select some checks for the tool to perform when using this configuration.'}
                                actions={[
                                    {
                                        title: 'OK',
                                        action: (() => this.setState({...this.state, emptyConfigError: false}))
                                    },
                                ]}
                                open={this.state.emptyConfigError}
                            />

                            <Alert
                                title={'Inverse Checks Selected'}
                                content={'You have selected \'' + this.getInverseChecks()[0] + '\' and \'' + this.getInverseChecks()[1] + '\'. Since these checks are the inverse of one another, please select at most one of them.'}
                                actions={[
                                    {
                                        title: 'OK',
                                        action: (() => this.setState({...this.state, inverseError: false}))
                                    },
                                ]}
                                open={this.state.inverseError}
                            />

                        </div>
                    </div>
                </Slide>
            </div>
        )
    }
}

export default NewConfig