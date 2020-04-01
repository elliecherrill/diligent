import React from 'react'
import initialConfigs from '../../../constants/config'
import Column from './Column'
import {DragDropContext} from 'react-beautiful-dnd'
import styled from 'styled-components'

const Container = styled.div`
    display: flex;
    
`

class NewConfig extends React.Component {
    state = initialConfigs

    // onDragStart = () => {
    //     document.body.style.color = 'orange'
    //     document.body.style.transition = 'background-color 0.2s ease'
    // }
    //
    // onDragUpdate = update => {
    //     const {destination} = update
    //     const opacity = destination
    //         ? destination.index / Object.keys(this.state.configs).length
    //         : 0
    //
    //     document.body.style.backgroundColor = `rgba(153, 141, 217, ${opacity})`
    // }

    onDragEnd = result => {
        // document.body.style.color = 'inherit'
        // document.body.style.backgroundColor = 'inherit'

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

    render() {
        return (
            <DragDropContext
                onDragEnd={this.onDragEnd}
                // onDragStart={this.onDragStart}
                // onDragUpdate={this.onDragUpdate}
            >
                <Container>
                    {this.state.columnOrder.map((columnId) => {
                        const column = this.state.categories[columnId]
                        const configs = column.configIds.map(configId => this.state.configs[configId])

                        return <Column key={column.id} column={column} configs={configs}/>
                    })}
                </Container>
            </DragDropContext>
        )
    }
}

export default NewConfig