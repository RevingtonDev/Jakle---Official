import DOMParserReact from "dom-parser-react";
import React, { Component } from "react";
import { createRef } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import { delete_messages, remove_friend } from "../api/request";
import { ENDPOINTS } from "../api/routes";
import { execute } from "../store";
import { PersonHeart } from "./Images";

export const Chat = (props) => {
  const params = useParams();
  return (
    <ChatComponent
      navigate={useNavigate()}
      params={params}
      {...props}
      dispatch={useDispatch()}
      user={useSelector((state) => state.reducer).data.find(
        (elm) => elm.id === params.id
      )}
      messages={useSelector((state) => state.reducer).messages[params.id]}
    />
  );
};

class ChatComponent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      errors: [],
      isWaiting: false,
    };

    this.chatSection = createRef();
    this.chatBox = createRef();
    this.messages = this.props.messages && this.props.messages.length;
  }

  componentDidMount() {
    this.scrollChatSection();

    const { id } = this.props.params;

    this.props.dispatch(execute({ type: "set_id", payload: id }));
    this.props.dispatch(
      execute({ type: "recieve", payload: { id: id, data: "clear" } })
    );
    this.props.dispatch(
      execute({
        type: "send",
        payload: {
          dest: ENDPOINTS.OPEN,
          data: { action: "active", content: id },
        },
      })
    );

    delete_messages(id);

    this.sendActivity(false);
  }

  componentDidUpdate(props) {
    if (props.messages && props.messages.length !== this.messages) {
      this.scrollChatSection();
      this.messages = props.messages.length;
    }
  }

  componentWillUnmount() {
    this.props.dispatch(execute({ type: "set_id", payload: 0 }));
    this.props.dispatch(
      execute({
        type: "send",
        payload: {
          dest: ENDPOINTS.OPEN,
          data: { action: "active", content: "" },
        },
      })
    );
  }

  scrollChatSection() {
    this.chatSection.current.scrollTo(0, this.chatSection.current.scrollHeight);
  }

  sendActivity(activity) {
    this.props.dispatch(
      execute({
        type: "send",
        payload: {
          dest: ENDPOINTS.TYPING,
          data: {
            action: "active",
            content: { id: this.props.params.id, activity: activity },
          },
        },
      })
    );
  }

  sendMessage() {
    if (this.chatBox.current.value !== "") {
      this.props.dispatch(
        execute({
          type: "message_send",
          payload: {
            action: "message_send",
            content: {
              id: this.props.params.id,
              content: `<p>${this.chatBox.current.value.replaceAll(
                "\n",
                "<br/>"
              )}</p>`,
            },
          },
        })
      );
      this.chatBox.current.value = "";
      this.scrollChatSection();
    }
  }

  async remove() {
    this.setState({ isWaiting: true });
    const req = await remove_friend(this.props.params.id);

    if (req && req.code === 200) {
      this.props.dispatch(
        execute({ type: "delete_friend", payload: this.props.params.id })
      );
    }
    this.setState({ isWaiting: false });
    this.props.navigate("../");
  }

  render() {
    const { user, messages } = this.props;
    const { errors, isWaiting } = this.state;
    return (
      <>
        <section
          className="chat-section container center parent-fill justify-start column-flex"
          style={{ padding: "0" }}
        >
          <div className="chat-header row center space-bet">
            <button
              onClick={() => {
                this.props.navigate("../");
              }}
              className="back-btn"
              style={{ fontSize: "2.5rem" }}
            >
              arrow_back
            </button>
            <div
              className="row-logo center"
              style={user && !user.profilePic ? { padding: "8px" } : {}}
            >
              {user && user.profilePic ? (
                <img src={user.profilePic} alt="profile-pic" />
              ) : (
                PersonHeart
              )}
              {user && user.active && !user.time ? (
                <>
                  <span className="status online"></span>
                </>
              ) : (
                ""
              )}
            </div>
            <div className="row-info center column-flex space-bet align-start">
              <div className="row-name capsule">{user && user.name}</div>
              <div className={"row-activity capsule online"}>
                {user && user.typing ? "Typing..." : ""}
              </div>
            </div>
            <div className="chat-controlls">
              <button
                className="btn default-btn"
                disabled={isWaiting}
                onClick={() => {
                  if (!isWaiting) this.remove();
                }}
              >
                Remove
              </button>
            </div>
          </div>
          <div
            ref={this.chatSection}
            className="message-section-container parent-fill"
          >
            {errors &&
              errors.map((elm) => (
                <>
                  <div className="section-message row center">{elm}</div>
                </>
              ))}
            <div className="message-section">
              {messages &&
                messages.map((elm) => {
                  return (
                    <>
                      <div className="message-container row">
                        <div
                          className={
                            "message " + (elm.sent ? "sent" : "recieved")
                          }
                        >
                          {<DOMParserReact source={elm.content} />}
                        </div>
                      </div>
                    </>
                  );
                })}
              {user && user.typing ? (
                <>
                  <div className="message-contaner row">
                    <div className="typing center space-bet show pop-animation message recieved">
                      <span className="typing-inside"></span>
                      <span className="typing-inside"></span>
                      <span className="typing-inside"></span>
                    </div>
                  </div>
                </>
              ) : (
                ""
              )}
            </div>
          </div>
          <div className="chat-footer row">
            <div className="data-input">
              <textarea
                disabled={isWaiting}
                ref={this.chatBox}
                onFocus={() => {
                  this.sendActivity(true);
                }}
                onBlur={() => {
                  this.sendActivity(false);
                }}
                onKeyDown={(e) => {
                  if (!e.shiftKey && e.key.toLowerCase() === "enter") {
                    e.preventDefault();
                    this.sendMessage();
                  }
                }}
                type="text"
                className="data-field row"
                max={300}
                pattern="*"
                placeholder="Message"
                spellCheck={false}
                required
              />
              <button
                disabled={isWaiting}
                onClick={() => {
                  this.sendMessage();
                }}
                className="btn default-btn"
              >
                Send
              </button>
            </div>
          </div>
        </section>
      </>
    );
  }
}
