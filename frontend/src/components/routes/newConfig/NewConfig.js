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
    Menu, Fade
} from '@material-ui/core'
import * as API from '../../../api'
import routes from '../../../constants/routes'
import {Redirect} from 'react-router-dom'
import {
    NoteAdd as AddDetailIcon,
    Clear as DeleteIcon
} from '@material-ui/icons'

const Container = styled.div`
    display: flex;
`

class NewConfig extends React.Component {
    state = {
        ...initialConfigs,
        title: '',
        titleError: false,
        goToHome: false,
        anchorEl: null,

        addCourseCode: false,
        courseCode: '',
        addExerciseNum: false,
        exerciseNum: '',
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
        finishConfigIds.splice(source.index, 0, draggableId)
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

    isValid = () => {
        //TODO: Add more validity checks - have they already got one with this name? have they selected any checks? (don't want to create an empty config)
        if (this.state.title === '') {
            const newState = {
                ...this.state,
                titleError: true
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
        })
    }

    render() {
        document.body.style.backgroundColor = colours.PRIMARY
        return (
            <div>
                <Slide direction="down" in={true} mountOnEnter unmountOnExit>
                    <div style={{paddingLeft: '5%', paddingRight: '5%'}}>
                        <DragDropContext
                            onDragEnd={this.onDragEnd}
                        >
                            <Container>
                                {this.state.columnOrder.map((columnId) => {
                                    const column = this.state.categories[columnId]
                                    const configs = column.configIds.map(configId => this.state.configs[configId])

                                    return <Column key={column.id} column={column} configs={configs}/>
                                })}
                            </Container>
                        </DragDropContext>
                    </div>
                </Slide>
                <Slide direction="up" in={true} mountOnEnter unmountOnExit>
                    <div style={{backgroundColor: 'white', padding: '5%', marginTop: '5%'}}>
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
                                    <Tooltip title="Add More Details">
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
                                        id="simple-menu"
                                        anchorEl={this.state.anchorEl}
                                        keepMounted
                                        open={Boolean(this.state.anchorEl)}
                                        onClose={this.handleMenuClose}
                                    >
                                        {!this.state.addCourseCode && <MenuItem onClick={this.addCourseCode}>Add Course Code</MenuItem>}
                                        {!this.state.addExerciseNum && <MenuItem onClick={this.addExerciseNum}>Add Exercise Number</MenuItem>}
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
                                    SAVE CONFIGURATION
                                </Button>
                            </div>

                            {this.state.goToHome &&
                            <Redirect push to={{
                                pathname: routes.HOME,
                                state: {title: this.state.title, new: true, edit: false}
                            }}/>}
                        </div>
                    </div>
                </Slide>
            </div>
        )
    }
}

export default NewConfig