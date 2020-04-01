import React from 'react'
import styled from 'styled-components'
import {Draggable} from 'react-beautiful-dnd'

const Container = styled.div`
    margin-bottom: 8px;
    padding: 8px;
    border: 1px solid lightgrey;
    border-radius: 2px;
    background-color: ${props => (props.isDragging ? 'lightgreen' : 'white')};
`

export default class ConfigComponent extends React.Component {
    render() {
        return (
            <Draggable draggableId={this.props.config.id} index={this.props.index}>
                {(provided, snapshot) => (
                    <Container
                        {...provided.draggableProps}
                        {...provided.dragHandleProps}
                        innerRef={provided.innerRef}
                        ref={provided.innerRef}
                        isDragging={snapshot.isDragging}
                    >
                        {this.props.config.content}
                    </Container>
                )}
            </Draggable>
        )
    }
}