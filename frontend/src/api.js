import authConstants from './constants/auth'
import axios from 'axios'
import moment from 'moment'

/**
 *
 * REST API
 *
 */
function getHeaders() {
    return {
        'Authorization': authConstants.ACCESS_TOKEN_HEADER()
    }
}

// export function get_current_time_as_moment() {
//     const axiosInstance = axios.create({})
//
//     // Use interceptors to work out time between sending request and receiving response
//     axiosInstance.interceptors.request.use((request) => {
//         request.ts = performance.now()
//         return request
//     })
//
//     axiosInstance.interceptors.response.use((response) => {
//         response.requestDurationMillis = Number(performance.now() - response.config.ts)
//         return response
//     })
//
//     // Use the requestDuration to work out what real time was when we sent the request
//     return axiosInstance.get('/api/quiz/server_time', {headers: getHeaders()})
//         .then(resp => moment(resp.data.time - resp.requestDurationMillis))
// }
//
// export function get_student_relevant_quizzes(login) {
//     return axios.get('/api/quiz/student', {params: {login: login}}, {headers: getHeaders()})
//         .then(resp => resp.data)
// }
//
// export function get_lecturer_relevant_quizzes(login) {
//     return axios.get('/api/quiz/lecturer', {params: {login: login}}, {headers: getHeaders()})
//         .then(resp => resp.data)
// }
//
// export function get_student_current_courses(login) {
//     return axios.get('/api/course/student', {params: {login: login}}, {headers: getHeaders()})
//         .then(resp => resp.data)
// }
//
// export function get_lecturer_current_courses(login) {
//     return axios.get('/api/course/lecturer', {params: {login: login}}, {headers: getHeaders()})
//         .then(resp => resp.data)
// }
//
// export function current_year(login) {
//     return axios.get('/api/currentyear', {params: {login: login}}, {headers: getHeaders()})
//         .then(resp => resp.data)
// }
//
// export function set_diagram_cluster_correct(quiz_id, question_id, clusters) {
//     let quiz = {
//         'quiz_id': quiz_id,
//         'question_id': question_id,
//         'clusters': clusters,
//     }
//
//     return axios.post('/api/quiz/setclustercorrect', quiz, {headers: getHeaders()})
// }
//
// export function get_correct_diagram_clusters(quiz_id, question_id) {
//     return axios.get('/api/quiz/getcorrectclusters/' + quiz_id + '/' + question_id, {headers: getHeaders()})
//         .then(resp => resp.data)
// }
//
// export function set_diagram_answers_correct(quiz_id, question_id, corrects) {
//     let quiz = {
//         'quiz_id': quiz_id,
//         'question_id': question_id,
//         'corrects': corrects,
//     }
//
//     return axios.post('/api/quiz/setdiagramanswerscorrect', quiz, {headers: getHeaders()})
// }
//
// export function create_new_quiz(title, questions, course, owner) {
//
//     let quiz = {
//         'title': title,
//         'course_id': course,
//         'owner_id': owner,
//         'questions': questions.map(q => q.formattedForAPI())
//     }
//
//     return axios.post('/api/quiz/new_quiz', quiz, {headers: getHeaders()})
// }
//
// export function get_quiz(quiz_id) {
//     return axios.get('/api/quiz/' + quiz_id, {headers: getHeaders()}).then(resp => resp.data)
// }
//
//
// export function advance_to_next_question(quiz_id) {
//     return axios.post('/api/quiz/' + quiz_id + '/advance', {headers: getHeaders()}).then(resp => resp.data)
// }
//
// export function delete_quiz(quiz_id) {
//     return axios.delete('/api/quiz/' + quiz_id, {headers: getHeaders()}).then(resp => resp.data)
// }
//
// export function get_individual_quiz_history(quiz_id) {
//     return axios.get('/api/quiz/' + quiz_id + '/history/individual', {headers: getHeaders()}).then(resp => resp.data)
// }
//
// export function get_group_quiz_history(quiz_id) {
//     return axios.get('/api/quiz/' + quiz_id + '/history/group', {headers: getHeaders()}).then(resp => resp.data)
// }
//
// export function get_diagram_analysis(quiz_id) {
//
//     let quiz = {
//         'quiz_id': quiz_id
//     }
//
//     return axios.post('/api/quiz/submit_diagram_analysis', quiz, {headers: getHeaders()})
// }
//
// export function submit_answer(answer_id, quiz_id, question_id) {
//
//     let response = {
//         'answer_id': answer_id,
//         'quiz_id': quiz_id,
//         'question_id': question_id
//     }
//
//     return axios.post('/api/quiz/responses', response, {headers: getHeaders()})
// }
//
// export function submit_free_text_response(answer, quiz_id, question_id) {
//
//     let response = {
//         'text': answer,
//         'quiz_id': quiz_id,
//         'question_id': question_id
//     }
//
//     return axios.post('/api/quiz/free_text_responses', response, {headers: getHeaders()})
//
// }
//
// export function submit_wordcloud_response(answer, quiz_id, question_id) {
//
//     let response = {
//         'text': answer,
//         'quiz_id': quiz_id,
//         'question_id': question_id
//     }
//
//     return axios.post('/api/quiz/wordcloud_responses', response, {headers: getHeaders()})
//
// }
//
//
// export function submit_diagram(diagram, quiz_id, question_id) {
//
//     let response = {
//         'quiz_id': quiz_id,
//         'question_id': question_id,
//         'diagram': diagram,
//     }
//
//
//     return axios.post('/api/quiz/diagram_responses', response, {headers: getHeaders()})
// }
//
// export function submit_drop_a_pin_response(quiz_id, question_id, pin_coords) {
//
//     let response = {
//         'quiz_id': quiz_id,
//         'question_id': question_id,
//         'pin_coords': pin_coords,
//     }
//
//     return axios.post('/api/quiz/drop_a_pin_responses', response, {headers: getHeaders()})
// }
//
// export function get_static_question_info(quiz_id) {
//     return axios.get('/api/quiz/' + quiz_id + '/current_question', {headers: getHeaders()}).then(resp => resp.data)
// }
//
// export function get_current_question_responses(quiz_id) {
//     return axios.get('/api/quiz/' + quiz_id + '/current_responses', {headers: getHeaders()}).then(resp => resp.data)
// }
//
// export function get_current_question_individual_response(quiz_id) {
//     return axios.get('/api/quiz/' + quiz_id + '/current_responses/individual', {headers: getHeaders()}).then(resp => resp.data)
// }
//
// export function get_current_question_clusters(quiz_id) {
//     return axios.get('/api/quiz/' + quiz_id + '/current_clusters', {headers: getHeaders()}).then(resp => resp.data)
// }
//
// export function show_answer(quiz_id) {
//     return axios.post('/api/quiz/show_answer/' + quiz_id, {}, {headers: getHeaders()})
// }