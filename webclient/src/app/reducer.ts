import {AnyAction, combineReducers, Store} from 'redux'

interface State1 {
    counter: number
}

interface State2 {
    s: string
}

export interface AppStore extends Store<any, AnyAction> {
    state1: State1;
    state2: State2;
}

const initialState1: State1 = {counter: 1}
const initialState2: State2 = {s: "2"}

function reducer1(state: State1 = initialState1, action: any): State1 {
    return {...state, counter: state.counter + 1}
}

function reducer2(state: State2 = initialState2, action: any): State2 {
    return state
}

export default combineReducers({
    state1: reducer1,
    state2: reducer2
})
