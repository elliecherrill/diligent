import React from 'react'
import initialConfigs from '../../../constants/config'
import Column from './Column'
import {DragDropContext} from 'react-beautiful-dnd'
import styled from 'styled-components'
import colours from '../../../constants/colours'
import {Button, TextField} from '@material-ui/core'
import * as API from '../../../api'
import routes from '../../../constants/routes'
import {Redirect} from 'react-router-dom'

const Container = styled.div`
    display: flex;
`

class NewConfig extends React.Component {
    state = {...initialConfigs, title: '', titleError: false, goToHome: false}

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
        const configIds = this.state.categories['category-1'].configIds

        return configIds.map((configId) => {
            return this.state.configs[configId].content
        })
    }

    getMediumPriorityChecks = () => {
        const configIds = this.state.categories['category-2'].configIds

        return configIds.map((configId) => {
            return this.state.configs[configId].content
        })
    }

    getLowPriorityChecks = () => {
        const configIds = this.state.categories['category-3'].configIds

        return configIds.map((configId) => {
            return this.state.configs[configId].content
        })
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

    render() {
        document.body.style.backgroundColor = colours.PRIMARY
        return (
            <div>
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
                <div style={{width: '100%', backgroundColor: 'white', padding: '5%', marginTop: '5%'}}>
                    <div style={{display: 'flex', justifyContent: 'center'}}>
                        <TextField
                            required
                            label='Configuration Name'
                            variant='outlined'
                            color='primary'
                            style={{marginRight: '2%'}}
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
                        <Button
                            variant='contained'
                            color='primary'
                            onClick={() => {
                                if (this.isValid()) {
                                    API.create_new_config(this.state.title, this.getHighPriorityChecks(), this.getMediumPriorityChecks(), this.getLowPriorityChecks()).then(() => {
                                        const newState = {
                                            ...this.state,
                                            goToHome: true
                                        }
                                        this.setState(newState)
                                    })
                                }
                            }}
                        >
                            Save Configuration
                        </Button>

                        {this.state.goToHome ? <Redirect to={{pathname: routes.HOME, state: this.state.title}}/> : false}
                    </div>
                </div>
            </div>
        )
    }
}

export default NewConfig