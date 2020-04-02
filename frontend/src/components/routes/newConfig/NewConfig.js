import React from 'react'
import initialConfigs from '../../../constants/config'
import Column from './Column'
import {DragDropContext} from 'react-beautiful-dnd'
import styled from 'styled-components'
import colours from '../../../constants/colours'
import {Button} from '@material-ui/core'
import * as API from '../../../api'

const Container = styled.div`
    display: flex;
    
`

class NewConfig extends React.Component {
    state = initialConfigs

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

    render() {
        document.body.style.backgroundColor = colours.PRIMARY
        return (
            <div>
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
                <div style={{display: 'flex', justifyContent: 'center', marginTop: '5%'}}>
                    <Button
                        variant='contained'
                        color='secondary'
                        onClick={() => {
                            //Add validity check of title etc
                            const title = "this is my config"
                            API.create_new_config(title, this.getHighPriorityChecks(), this.getMediumPriorityChecks(), this.getLowPriorityChecks())
                            //.then() create snackbar
                        }}
                    >
                        Save Configuration
                    </Button>
                </div>
            </div>
        )
    }
}

export default NewConfig