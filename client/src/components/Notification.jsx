import React, { Component } from "react";
import { get_info, respond_to_request } from "../api/request";
import { PersonCheck, PersonHeart } from "./Images";
import { execute } from "../store";
import { useDispatch } from "react-redux";

export const Notification = (props) => {
  return <NotificationsComponent {...props} dispatch={useDispatch()} />;
};

class NotificationsComponent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isWaiting: false,
      accepted: undefined,
    };
    if (this.props.retrieveOnMount)
      this.get_info(this.props.notification.notification.second);
  }

  async get_info(id) {
    this.setState({ isWaiting: true });
    const req = await get_info(id);
    if (req) {
      this.setState({ user: req.results });
    }
    this.setState({ isWaiting: false });
  }

  async respondToRequest(id, res) {
    if (id) {
      this.setState({ isWaiting: true });
      const req = await respond_to_request(id, res);
      if (req) {
        if (res === "yes") {
          this.props.dispatch(
            execute({ type: "add_friend", payload: req.results })
          );
          this.setState({ accepted: true });
        } else {
          this.setState({ accepted: false });
        }
      }
      this.setState({ isWaiting: false });
    }
  }

  render() {
    const { notification } = this.props;
    const { isWaiting, accepted } = this.state;

    if (notification.category === "F_R_A") {
      const user = notification.detail;
      return (
        <>
          <section
            className={
              "notification row center rel " + (isWaiting ? "" : "space-bet")
            }
          >
            {isWaiting || !user ? (
              <>
                <div className="ld-c center rel">
                  <div className="component-waiting center space-bet align-start column-flex parent-fill">
                    <span className="loading-bar capsule"></span>
                    <span className="loading-bar capsule"></span>
                    <span className="loading-bar capsule"></span>
                  </div>
                </div>
              </>
            ) : (
              <></>
            )}{" "}
            <div className="info row center row-flex">
              <div
                className="row-logo"
                style={!user.profilePic ? { padding: "8px" } : {}}
              >
                {user && user.profilePic ? (
                  <img src={user.profilePic} alt="profile-pic" />
                ) : (
                  PersonHeart
                )}
              </div>
              <div className="row-info">
                <div className="title row-name">{user.name}</div>
                <div className="n-content row-activity">
                  Accepted your friend request.
                </div>
              </div>
            </div>
            <div className="end-corner abs show-time">
              {new Date(notification.time).toLocaleDateString()}
            </div>
          </section>
        </>
      );
    }

    if (notification.category === "F_R_") {
      const user = notification.detail;
      return (
        <>
          <section
            className={
              "notification row center rel " + (isWaiting ? "" : "space-bet")
            }
          >
            {isWaiting || !user ? (
              <>
                <div className="ld-c center rel">
                  <div className="component-waiting center space-bet align-start column-flex parent-fill">
                    <span className="loading-bar capsule"></span>
                    <span className="loading-bar capsule"></span>
                    <span className="loading-bar capsule"></span>
                  </div>
                </div>
              </>
            ) : (
              <></>
            )}
            <div className="info row center row-flex">
              <div
                className="row-logo"
                style={!user.profilePic ? { padding: "8px" } : {}}
              >
                {user && user.profilePic ? (
                  <img src={user.profilePic} alt="profile-pic" />
                ) : (
                  PersonHeart
                )}
              </div>
              <div className="row-info">
                <div className="title row-name">{user.name}</div>
                <div className="n-content row-activity">
                  Sent you a friend request.
                </div>
              </div>
            </div>
            <div className="row-actions center row-flex">
              {accepted === undefined ? (
                <>
                  <button
                    className="icon green btn"
                    onClick={() => {
                      this.respondToRequest(
                        notification.notification.second,
                        "yes"
                      );
                    }}
                  >
                    check
                  </button>
                  <button
                    className="icon red btn"
                    onClick={() => {
                      this.respondToRequest(
                        notification.notification.second,
                        "no"
                      );
                    }}
                  >
                    close
                  </button>
                </>
              ) : accepted ? (
                <span className="info-logo center">{PersonCheck}</span>
              ) : (
                <></>
              )}
            </div>
            <div className="end-corner abs show-time">
              {new Date(notification.time).toLocaleDateString()}
            </div>
          </section>
        </>
      );
    }

    return (
      <>
        <section
          className={
            "notification row center rel " + (isWaiting ? "" : "space-bet")
          }
        >
          {isWaiting ? (
            <>
              <div className="ld-c center">
                <div className="component-waiting center space-bet align-start column-flex parent-fill">
                  <span className="loading-bar capsule"></span>
                  <span className="loading-bar capsule"></span>
                  <span className="loading-bar capsule"></span>
                </div>
              </div>
            </>
          ) : (
            <></>
          )}
          <div className="info row center row-flex">
            <div className="row-logo center">
              <span className="icon center red">warning</span>
            </div>
            <div className="row-info">
              <div className="title">{notification.notification.first}</div>
              <div className="n-content">
                {notification.notification.second}
              </div>
            </div>
          </div>
          <div className="end-corner abs show-time">
            {new Date(notification.time).toLocaleDateString()}
          </div>
        </section>
      </>
    );
  }
}
