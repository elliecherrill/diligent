import React from 'react'
import styled from 'styled-components'
import {Draggable} from 'react-beautiful-dnd'
import colours from '../../../constants/colours'

const Container = styled.div`
    margin-bottom: 8px;
    padding: 8px;
    border: 3px solid ${props => ((props.isDragging || props.searchResult) ? colours.PRIMARY : 'lightgray')};
    border-radius: 10px;
    background-color: white;
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
                        searchResult={this.props.searchResult}
                    >
                        {this.props.config.content}
                    </Container>
                )}
            </Draggable>
        )
    }
}