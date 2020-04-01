import React from 'react'
import styled from 'styled-components'
import ConfigComponent from "./ConfigComponent";
import {Draggable, Droppable} from "react-beautiful-dnd";

const Container = styled.div`
    margin: 8px;
    border: 1px solid lightgrey;
    border-radius: 2px;
    width: 100%;
    
    display: flex;
    flex-direction: column;
`
const Title = styled.h3`
    padding: 8px;
`
const ConfigList = styled.div`
    padding: 8px;
    background-color: ${props => (props.isDraggingOver ? 'skyblue' : 'white')};
    transition: background-color 0.2s ease;
    flex-grow: 1;
    min-height: 100px;
`

class InnerList extends React.Component {
    shouldComponentUpdate(nextProps) {
        return nextProps.configs !== this.props.configs
    }

    render() {
        return this.props.configs.map((config, index) => (
            <ConfigComponent key={config.id} config={config} index={index}/>
        ))
    }
}

export default class Column extends React.Component {
    render() {
        return (
            <Container>
                <Title>{this.props.column.title}</Title>
                <Droppable droppableId={this.props.column.id}>
                    {(provided, snapshot) => (
                        <ConfigList
                            innerRef={provided.innerRef}
                            ref={provided.innerRef}
                            {...provided.droppableProps}
                            isDraggingOver={snapshot.isDraggingOver}
                        >
                            <InnerList configs={this.props.configs}/>
                            {provided.placeholder}
                        </ConfigList>
                    )}
                </Droppable>
            </Container>
        )
    }
}
